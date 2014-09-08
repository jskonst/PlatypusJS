/* Datamodel license.
 * Exclusive rights on this code in any form
 * are belong to it's author. This code was
 * developed for commercial purposes only. 
 * For any questions and any actions with this
 * code in any form you have to contact to it's
 * author.
 * All rights reserved.
 */
package com.eas.client;

import com.bearsoft.rowset.Converter;
import com.bearsoft.rowset.Rowset;
import com.bearsoft.rowset.changes.Change;
import com.bearsoft.rowset.changes.EntitiesHost;
import com.bearsoft.rowset.dataflow.FlowProvider;
import com.bearsoft.rowset.dataflow.TransactionListener;
import com.bearsoft.rowset.exceptions.ResourceUnavalableException;
import com.bearsoft.rowset.jdbc.JdbcReader;
import com.bearsoft.rowset.jdbc.StatementsGenerator;
import com.bearsoft.rowset.jdbc.StatementsGenerator.StatementsLogEntry;
import com.bearsoft.rowset.metadata.*;
import com.eas.client.login.DbPlatypusPrincipal;
import com.eas.client.login.PlatypusPrincipal;
import com.eas.client.login.PrincipalHost;
import com.eas.client.queries.ContextHost;
import com.eas.client.queries.QueriesProxy;
import com.eas.client.resourcepool.GeneralResourceProvider;
import com.eas.client.sqldrivers.SqlDriver;
import com.eas.concurrent.CallableConsumer;
import com.eas.util.ListenerRegistration;
import com.eas.util.StringUtils;
import java.security.AccessControlException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Two tier implementation of Client interface.
 *
 * @author mg
 * @see Client
 */
public class DatabasesClient {

    public static final String BAD_LOGIN_MSG = "Login incorrect";
    private static final String USERNAME_PARAMETER_NAME = "userName";
    private static final String SELECT_BY_FIELD_QUERY = "select * from %s where Upper(%s) = :%s";
    private static final String USER_QUERY_TEXT = String.format(SELECT_BY_FIELD_QUERY, ClientConstants.T_MTD_USERS, ClientConstants.F_USR_NAME, USERNAME_PARAMETER_NAME);
    private static final String USER_GROUPS_QUERY_TEXT = String.format(SELECT_BY_FIELD_QUERY, ClientConstants.T_MTD_GROUPS, ClientConstants.F_USR_NAME, USERNAME_PARAMETER_NAME);
    public static final String TYPES_INFO_TRACE_MSG = "Getting types info. DbId %s";
    public static final String USER_MISSING_MSG = "No user found (%s)";
    // metadata
    protected Map<String, DatabaseMdCache> mdCaches = new HashMap<>();
    protected boolean autoFillMetadata = true;
    // callback interface for context
    protected ContextHost contextHost;
    // callback interface for principal
    protected PrincipalHost principalHost;
    // transactions
    protected final Set<TransactionListener> transactionListeners = new CopyOnWriteArraySet<>();
    // datasource name used by default. E.g. in queries with null datasource name
    protected String defaultDatasourceName;
    protected QueriesProxy<SqlQuery> queries;
    protected ExecutorService commitProcessor = Executors.newCachedThreadPool();

    /**
     *
     * @param aDefaultDatasourceName
     * @param aAutoFillMetadata If true, metadatacache will be filled with
     * tables, keys and other metadata in schema automatically. Otherwise it
     * will query metadata table by table in each case. Default is true.
     * @throws Exception
     */
    public DatabasesClient(String aDefaultDatasourceName, boolean aAutoFillMetadata) throws Exception {
        super();
        defaultDatasourceName = aDefaultDatasourceName;
        autoFillMetadata = aAutoFillMetadata;
    }

    public QueriesProxy<SqlQuery> getQueries() {
        return queries;
    }

    public void setQueries(QueriesProxy<SqlQuery> aQueries) {
        queries = aQueries;
    }

    public String getDefaultDatasourceName() {
        return defaultDatasourceName;
    }

    public void setDefaultDatasourceName(String aValue) throws Exception {
        setDefaultDatasourceName(aValue, true);
    }

    public void setDefaultDatasourceName(String aValue, boolean fireEvents) throws Exception {
        if (defaultDatasourceName == null ? aValue != null : !defaultDatasourceName.equals(aValue)) {
            String oldDefaultDatasourceName = defaultDatasourceName;
            DatabaseMdCache oldMdCache = mdCaches.remove(null);
            DatabaseMdCache newMdCache = mdCaches.remove(aValue);
            defaultDatasourceName = aValue;
            if (oldMdCache != null) {
                mdCaches.put(oldDefaultDatasourceName, oldMdCache);
            }
            if (newMdCache != null) {
                mdCaches.put(null, newMdCache);
            }
        }
    }

    public boolean isAutoFillMetadata() {
        return autoFillMetadata;
    }

    public DataSource obtainDataSource(String aDataSourceId) throws Exception {
        if (aDataSourceId == null) {
            aDataSourceId = defaultDatasourceName;
        }
        if (aDataSourceId != null) {
            Context initContext = new InitialContext();
            try {
                // J2EE servers
                return (DataSource) initContext.lookup(aDataSourceId);
            } catch (NamingException ex) {
                try {
                    // Apache Tomcat component's JNDI context 
                    Context envContext = (Context) initContext.lookup("java:/comp/env"); //NOI18N
                    return (DataSource) envContext.lookup(aDataSourceId);
                } catch (NamingException ex1) {
                    // Platypus standalone server or client
                    return GeneralResourceProvider.getInstance().getPooledDataSource(aDataSourceId);
                }
            }
        } else {
            throw new NamingException("Null datasource name is not allowed (Default datasource name is null also).");
        }
    }

    public ListenerRegistration addTransactionListener(final TransactionListener aListener) {
        transactionListeners.add(aListener);
        return () -> {
            transactionListeners.remove(aListener);
        };
    }

    /**
     * @param aContextHost <code>ConnectionPrepender</code> instance to be used
     * in flow providers, created by the client.
     */
    public void setContextHost(ContextHost aContextHost) {
        contextHost = aContextHost;
    }

    /**
     * Factory method for DatabaseFlowProvider. Intended to incapsulate flow
     * provider creation in two tier or three tier applications.
     *
     * @param aDatasourceId
     * @param aEntityId
     * @param aSqlClause
     * @param aExpectedFields
     * @param aReadRoles
     * @param aWriteRoles
     * @return FlowProvider created.
     * @throws Exception
     */
    public FlowProvider createFlowProvider(String aDatasourceId, String aEntityId, String aSqlClause, Fields aExpectedFields, Set<String> aReadRoles, Set<String> aWriteRoles) throws Exception {
        return new PlatypusJdbcFlowProvider(this, aDatasourceId, aEntityId, obtainDataSource(aDatasourceId), commitProcessor, getDbMetadataCache(aDatasourceId), aSqlClause, aExpectedFields, contextHost, aReadRoles, aWriteRoles);
    }

    public String getSqlLogMessage(SqlCompiledQuery query) {
        StringBuilder sb = new StringBuilder("Executing SQL query: ");
        sb.append(query.getSqlClause());
        if (query.getParameters().getParametersCount() > 0) {
            sb.append(" {");
            String delimiter = "";
            for (int i = 1; i <= query.getParameters().getParametersCount(); i++) {
                Parameter param = query.getParameters().get(i);
                sb.append(delimiter).append(Integer.toString(i)).append('@').append(param.getValue() == null ? "null" : param.getValue().toString());
                delimiter = ", ";
            }
            sb.append("}");
        }
        return sb.toString();
    }

    public static Map<String, String> getUserProperties(DatabasesClient aClient, String aUserName, Consumer<Map<String, String>> onSuccess, Consumer<Exception> onFailure) throws Exception {
        final SqlQuery q = new SqlQuery(aClient, USER_QUERY_TEXT);
        q.putParameter(USERNAME_PARAMETER_NAME, DataTypeInfo.VARCHAR, aUserName.toUpperCase());
        aClient.initUsersSpace(q.getDbId());
        SqlCompiledQuery compiled = q.compile();
        CallableConsumer<Map<String, String>, Rowset> doWork = (Rowset rs) -> {
            Map<String, String> properties = new HashMap<>();
            if (rs.first()) {
                properties.put(ClientConstants.F_USR_NAME, aUserName);
                properties.put(ClientConstants.F_USR_CONTEXT, rs.getString(rs.getFields().find(ClientConstants.F_USR_CONTEXT)));
                properties.put(ClientConstants.F_USR_EMAIL, rs.getString(rs.getFields().find(ClientConstants.F_USR_EMAIL)));
                properties.put(ClientConstants.F_USR_PHONE, rs.getString(rs.getFields().find(ClientConstants.F_USR_PHONE)));
                properties.put(ClientConstants.F_USR_FORM, rs.getString(rs.getFields().find(ClientConstants.F_USR_FORM)));
                properties.put(ClientConstants.F_USR_PASSWD, rs.getString(rs.getFields().find(ClientConstants.F_USR_PASSWD)));
            }
            return properties;
        };
        if (onSuccess != null) {
            compiled.executeQuery((Rowset rs) -> {
                try {
                    onSuccess.accept(doWork.call(rs));
                } catch (Exception ex) {
                    if (onFailure != null) {
                        onFailure.accept(ex);
                    }
                }
            }, onFailure);
            return null;
        } else {
            final Rowset rs = compiled.executeQuery(null, null);
            return doWork.call(rs);
        }
    }

    private static class UserInfo {

        public Map<String, String> props;
        public Set<String> roles;

        public DbPlatypusPrincipal principal(String aUserName) {
            if (roles != null && props != null) {
                return new DbPlatypusPrincipal(aUserName,
                        props.get(ClientConstants.F_USR_CONTEXT),
                        props.get(ClientConstants.F_USR_EMAIL),
                        props.get(ClientConstants.F_USR_PHONE),
                        props.get(ClientConstants.F_USR_FORM),
                        roles);
            } else {
                return null;
            }
        }
    }

    public static DbPlatypusPrincipal credentialsToPrincipalWithBasicAuthentication(DatabasesClient aClient, String aUserName, String password, Consumer<DbPlatypusPrincipal> onSuccess, Consumer<Exception> onFailure) throws Exception {
        final UserInfo ui = new UserInfo();
        if (onSuccess != null) {
            getUserProperties(aClient, aUserName, (Map<String, String> userProperties) -> {
                synchronized (ui) {
                    ui.props = userProperties;
                    if (ui.roles != null) {
                        if (password.equals(ui.props.get(ClientConstants.F_USR_PASSWD))) {
                            onSuccess.accept(ui.principal(aUserName));
                        } else {
                            onSuccess.accept(null);
                        }
                    }
                }
            }, onFailure);
            getUserRoles(aClient, aUserName, (Set<String> aRoles) -> {
                synchronized (ui) {
                    ui.roles = aRoles;
                    if (ui.props != null) {
                        if (password.equals(ui.props.get(ClientConstants.F_USR_PASSWD))) {
                            onSuccess.accept(ui.principal(aUserName));
                        } else {
                            onSuccess.accept(null);
                        }
                    }
                }
            }, onFailure);
            return null;
        } else {
            ui.props = getUserProperties(aClient, aUserName, null, null);
            ui.roles = getUserRoles(aClient, aUserName, null, null);
            if (password.equals(ui.props.get(ClientConstants.F_USR_PASSWD))) {
                return ui.principal(aUserName);
            } else {
                return null;
            }
        }
    }

    public static DbPlatypusPrincipal userNameToPrincipal(DatabasesClient aClient, String aUserName, Consumer<DbPlatypusPrincipal> onSuccess, Consumer<Exception> onFailure) throws Exception {
        final UserInfo ui = new UserInfo();
        if (onSuccess != null) {
            getUserProperties(aClient, aUserName, (Map<String, String> userProperties) -> {
                synchronized (ui) {
                    ui.props = userProperties;
                    if (ui.roles != null) {
                        onSuccess.accept(ui.principal(aUserName));
                    }
                }
            }, onFailure);
            getUserRoles(aClient, aUserName, (Set<String> aRoles) -> {
                synchronized (ui) {
                    ui.roles = aRoles;
                    if (ui.props != null) {
                        onSuccess.accept(ui.principal(aUserName));
                    }
                }
            }, onFailure);
            return null;
        } else {
            ui.props = getUserProperties(aClient, aUserName, null, null);
            ui.roles = getUserRoles(aClient, aUserName, null, null);
            return ui.principal(aUserName);
        }
    }

    public int executeUpdate(SqlCompiledQuery aQuery, Consumer<Integer> onSuccess, Consumer<Exception> onFailure) throws Exception {
        int rowsAffected = 0;
        Converter converter = getDbMetadataCache(aQuery.getDatabaseId()).getConnectionDriver().getConverter();
        DataSource dataSource = obtainDataSource(aQuery.getDatabaseId());
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection(); PreparedStatement stmt = connection.prepareStatement(aQuery.getSqlClause())) {
                connection.setAutoCommit(false);
                Parameters params = aQuery.getParameters();
                for (int i = 1; i <= params.getParametersCount(); i++) {
                    Parameter param = params.get(i);
                    converter.convert2JdbcAndAssign(param.getValue(), param.getTypeInfo(), connection, i, stmt);
                }
                try {
                    rowsAffected += stmt.executeUpdate();
                    connection.commit();
                } catch (SQLException ex) {
                    connection.rollback();
                    throw ex;
                }
            }
        }
        return rowsAffected;
    }

    public synchronized DatabaseMdCache getDbMetadataCache(String aDatasourceId) throws Exception {
        if (!mdCaches.containsKey(aDatasourceId)) {
            DatabaseMdCache cache = new DatabaseMdCache(this, aDatasourceId);
            mdCaches.put(aDatasourceId, cache);
            if (autoFillMetadata) {
                try {
                    cache.fillTablesCacheByConnectionSchema(true);
                } catch (ResourceUnavalableException ex) {
                    Logger.getLogger(DatabasesClient.class.getName()).log(Level.WARNING, ex.getMessage());
                }
            }
        }
        return mdCaches.get(aDatasourceId);
    }

    protected static class CommitProcess extends AsyncProcess<Integer> {

        public int rowsAffected;

        public CommitProcess(int aExpected, Consumer<Integer> aOnSuccess, Consumer<Exception> aOnFailure) {
            super(aExpected, aOnSuccess, aOnFailure);
        }

        /**
         *
         * @param aRowsAffected
         * @param aFailureCause
         */
        @Override
        public synchronized void complete(Integer aRowsAffected, Exception aFailureCause) {
            rowsAffected += aRowsAffected;
            if (aFailureCause != null) {
                exceptions.add(aFailureCause);
            }
            if (++completed == expected) {
                doComplete(rowsAffected);
            }
        }
    }

    public int commit(Map<String, List<Change>> aDatasourcesChangeLogs, Consumer<Integer> onSuccess, Consumer<Exception> onFailure) throws Exception {
        if (onSuccess != null) {
            if (!aDatasourcesChangeLogs.isEmpty()) {
                final CommitProcess ci = new CommitProcess(aDatasourcesChangeLogs.size(), (Integer rowsAffected) -> {
                    commited();
                    onSuccess.accept(rowsAffected != null ? rowsAffected : 0);
                }, onFailure);
                for (final String datasourceName : aDatasourcesChangeLogs.keySet()) {
                    List<Change> dbLog = aDatasourcesChangeLogs.get(datasourceName);
                    commit(datasourceName, dbLog, (Integer rowsAffected) -> {
                        ci.complete(rowsAffected != null ? rowsAffected : 0, null);
                    }, (Exception ex) -> {
                        ci.complete(0, ex);
                    });
                }
            } else {
                onSuccess.accept(0);
            }
            return 0;
        } else {
            int rowsAffected = 0;
            try {
                for (final String datasourceName : aDatasourcesChangeLogs.keySet()) {
                    List<Change> dbLog = aDatasourcesChangeLogs.get(datasourceName);
                    rowsAffected += commit(datasourceName, dbLog, null, null);
                }
                commited();
                return rowsAffected;
            } catch (Exception ex) {
                rollback();
                throw ex;
            }
        }
    }

    private void commited() {
        TransactionListener[] listeners = transactionListeners.toArray(new TransactionListener[]{});
        for (TransactionListener l : listeners) {
            try {
                l.commited();
            } catch (Exception ex) {
                Logger.getLogger(DatabasesClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected int commit(final String aDatasourceName, List<Change> aLog, Consumer<Integer> onSuccess, Consumer<Exception> onFailure) throws Exception {
        Callable<Integer> doWork = () -> {
            int rowsAffected = 0;
            SqlDriver driver = getDbMetadataCache(aDatasourceName).getConnectionDriver();
            if (driver != null) {
                Converter converter = driver.getConverter();
                assert aLog != null;
                DataSource dataSource = obtainDataSource(aDatasourceName);
                try (Connection connection = dataSource.getConnection()) {
                    connection.setAutoCommit(false);
                    try {
                        List<StatementsLogEntry> statements = new ArrayList<>();
                        // This structure helps us to avoid actuality check for queries while
                        // processing each statement in transaction. Thus, we can avoid speed degradation.
                        // It doesn't break security, because such "unactual" lookup takes place ONLY
                        // while transaction processing.
                        final Map<String, SqlQuery> entityQueries = new HashMap<>();
                        for (Change change : aLog) {
                            StatementsGenerator generator = new StatementsGenerator(converter, new EntitiesHost() {
                                @Override
                                public void checkRights(String aEntityId) throws Exception {
                                    if (queries != null && aEntityId != null) {
                                        SqlQuery query = entityQueries.get(aEntityId);
                                        if (query == null) {
                                            query = queries.getQuery(aEntityId, null, null);
                                            if (query != null) {
                                                entityQueries.put(aEntityId, query);
                                            }
                                        }
                                        if (query != null) {
                                            checkWritePrincipalPermission(aEntityId, query.getWriteRoles());
                                        }
                                    }
                                }

                                @Override
                                public Field resolveField(String aEntityId, String aFieldName) throws Exception {
                                    if (aEntityId != null) {
                                        SqlQuery query;
                                        if (queries != null) {
                                            query = entityQueries.get(aEntityId);
                                            if (query == null) {
                                                query = queries.getQuery(aEntityId, null, null);
                                                if (query != null) {
                                                    entityQueries.put(aEntityId, query);
                                                }
                                            }
                                        } else {
                                            query = null;
                                        }
                                        Fields fields;
                                        if (query != null && query.getEntityId() != null) {
                                            fields = query.getFields();
                                        } else {// It seems, that aEntityId is a table name...
                                            fields = mdCaches.get(aDatasourceName).getTableMetadata(aEntityId);
                                        }
                                        if (fields != null) {
                                            Field resolved = fields.get(aFieldName);
                                            String resolvedTableName = resolved != null ? resolved.getTableName() : null;
                                            resolvedTableName = resolvedTableName != null ? resolvedTableName.toLowerCase() : "";
                                            if (query != null && query.getWritable() != null && !query.getWritable().contains(resolvedTableName)) {
                                                return null;
                                            } else {
                                                return resolved;
                                            }
                                        } else {
                                            Logger.getLogger(DatabasesClient.class.getName()).log(Level.WARNING, "Cant find fields for entity id:{0}", aEntityId);
                                            return null;
                                        }
                                    } else {
                                        return null;
                                    }
                                }
                            }, ClientConstants.F_USR_CONTEXT, contextHost != null ? contextHost.preparationContext() : null);
                            change.accept(generator);
                            statements.addAll(generator.getLogEntries());
                        }
                        rowsAffected = riddleStatements(statements, connection);
                        connection.commit();
                    } catch (Exception ex) {
                        connection.rollback();
                        throw ex;
                    }
                }
                aLog.clear();
                return rowsAffected;
            } else {
                Logger.getLogger(DatabasesClient.class.getName()).log(Level.INFO, "Unknown datasource: {0}. Can't commit to it", aDatasourceName);
                return 0;
            }
        };
        if (onSuccess != null) {
            commitProcessor.submit(() -> {
                try {
                    int rowsAffected = doWork.call();
                    try {// We have to handle commit exceptions and onSuccess.accept() exceptions separatly.
                        onSuccess.accept(rowsAffected);
                    } catch (Exception ex) {
                        Logger.getLogger(DatabasesClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (Exception ex) {
                    if (onFailure != null) {
                        onFailure.accept(ex);
                    }
                }
            });
            return 0;
        } else {
            return doWork.call();
        }
    }

    private void checkWritePrincipalPermission(String aEntityId, Set<String> writeRoles) throws Exception {
        if (getPrincipalHost() != null && writeRoles != null && !writeRoles.isEmpty()) {
            PlatypusPrincipal principal = getPrincipalHost().getPrincipal();
            if (principal != null && principal.hasAnyRole(writeRoles)) {
                return;
            }
            throw new AccessControlException(String.format("Access denied for write (entity: %s) for '%s'.", aEntityId != null ? aEntityId : "", principal != null ? principal.getName() : null));
        }
    }

    public void rollback() {
        TransactionListener[] listeners = transactionListeners.toArray(new TransactionListener[]{});
        for (TransactionListener l : listeners) {
            try {
                l.rolledback();
            } catch (Exception ex) {
                Logger.getLogger(DatabasesClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Rowset getDbTypesInfo(String aDatasourceId) throws Exception {
        Logger.getLogger(DatabasesClient.class.getName()).fine(String.format(TYPES_INFO_TRACE_MSG, aDatasourceId));
        Rowset lrowSet = new Rowset();
        JdbcReader rsReader = new JdbcReader(getDbMetadataCache(aDatasourceId).getConnectionDriver().getConverter());
        DataSource dataSource = obtainDataSource(aDatasourceId);
        if (dataSource != null) {
            try (Connection lconn = dataSource.getConnection()) {
                DatabaseMetaData dbmd = lconn.getMetaData();
                if (dbmd != null) {
                    try (ResultSet rs = dbmd.getTypeInfo()) {
                        lrowSet = rsReader.readRowset(rs, -1);
                    }
                }
            }
        }
        return lrowSet;
    }

    public void dbTableChanged(String aDatasourceName, String aSchema, String aTable) throws Exception {
        DatabaseMdCache cache = getDbMetadataCache(aDatasourceName);
        String fullTableName = aTable;
        if (aSchema != null && !aSchema.isEmpty()) {
            fullTableName = aSchema + "." + fullTableName;
        }
        cache.removeTableMetadata(fullTableName);
        cache.removeTableIndexes(fullTableName);
    }

    public PrincipalHost getPrincipalHost() {
        return principalHost;
    }

    public void setPrincipalHost(PrincipalHost aPrincipalHost) {
        principalHost = aPrincipalHost;
    }

    public String getConnectionSchema(String aDatasourceId) throws Exception {
        DataSource ds = obtainDataSource(aDatasourceId);
        if (ds != null) {
            try (Connection conn = ds.getConnection()) {
                return schemaByConnection(conn);
            }
        } else {
            return null;
        }
    }

    public String getConnectionDialect(String aDatasourceId) throws Exception {
        DataSource ds = obtainDataSource(aDatasourceId);
        try (Connection conn = ds.getConnection()) {
            return dialectByConnection(conn);
        }
    }

    public SqlDriver getConnectionDriver(String aDatasourceId) throws Exception {
        DataSource ds = obtainDataSource(aDatasourceId);
        try (Connection conn = ds.getConnection()) {
            return SQLUtils.getSqlDriver(dialectByConnection(conn));
        }
    }

    protected static Set<String> getUserRoles(DatabasesClient aClient, String aUserName, Consumer<Set<String>> onSuccess, Consumer<Exception> onFailure) throws Exception {
        CallableConsumer<Set<String>, Rowset> doWork = (Rowset rs) -> {
            Set<String> roles = new HashSet<>();
            int roleNameColumnIndex = rs.getFields().find(ClientConstants.F_GROUP_NAME);
            for (int i = 1; i <= rs.size(); i++) {
                roles.add((String) rs.getRow(i).getColumnObject(roleNameColumnIndex));
            }
            return roles;
        };
        final SqlQuery q = new SqlQuery(aClient, USER_GROUPS_QUERY_TEXT);
        q.putParameter(USERNAME_PARAMETER_NAME, DataTypeInfo.VARCHAR, aUserName.toUpperCase());
        SqlCompiledQuery compiled = q.compile();
        if (onSuccess != null) {
            compiled.executeQuery((Rowset rs) -> {
                try {
                    onSuccess.accept(doWork.call(rs));
                } catch (Exception ex) {
                    if (onFailure != null) {
                        onFailure.accept(ex);
                    }
                }
            }, onFailure);
            return null;
        } else {
            Rowset rs = compiled.executeQuery(null, null);
            return doWork.call(rs);
        }
    }

    private int riddleStatements(List<StatementsLogEntry> aStatements, Connection aConnection) throws Exception {
        int rowsAffected = 0;
        if (!aStatements.isEmpty()) {
            List<StatementsLogEntry> errorStatements = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            for (StatementsLogEntry entry : aStatements) {
                try {
                    rowsAffected += entry.apply(aConnection);
                } catch (Exception ex) {
                    errorStatements.add(entry);
                    errors.add(ex.getMessage());
                    Logger.getLogger(DatabasesClient.class.getName()).log(Level.SEVERE, ex.getMessage());
                }
            }
            if (errorStatements.size() == aStatements.size()) {
                throw new SQLException(StringUtils.join(System.getProperty(ClientConstants.LINE_SEPARATOR_PROP_NAME), errors.toArray(new String[]{})));
            } else if (errorStatements.size() < aStatements.size()) {
                rowsAffected += riddleStatements(errorStatements, aConnection);
            }
        }
        return rowsAffected;
    }

    public static String dialectByConnection(Connection aConnection) throws SQLException {
        String dialect = SQLUtils.dialectByUrl(aConnection.getMetaData().getURL());
        if (dialect == null) {
            dialect = SQLUtils.dialectByProductName(aConnection.getMetaData().getDatabaseProductName());
        }
        return dialect;
    }

    public static String schemaByConnection(Connection aConnection) throws SQLException {
        String dialect = dialectByConnection(aConnection);
        SqlDriver driver = SQLUtils.getSqlDriver(dialect);
        if (driver != null) {
            String getSchemaClause = driver.getSql4GetConnectionContext();
            try (Statement stmt = aConnection.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(getSchemaClause)) {
                    if (rs.next() && rs.getMetaData().getColumnCount() > 0) {
                        return rs.getString(1);
                    }
                }
            }
        } else {
            Logger.getLogger(GeneralResourceProvider.class.getName()).log(Level.SEVERE, String.format("Can't obtain sql driver for %s", aConnection.toString()));
        }
        return null;
    }

    public void initUsersSpace(String aDatasourceName) throws Exception {
        if (aDatasourceName == null) {
            aDatasourceName = defaultDatasourceName;
        }
        initUsersSpace(obtainDataSource(aDatasourceName));
    }

    public static void initUsersSpace(DataSource aSource) throws Exception {
        try (Connection lconn = aSource.getConnection()) {
            lconn.setAutoCommit(false);
            String dialect = dialectByConnection(lconn);
            SqlDriver driver = SQLUtils.getSqlDriver(dialect);
            driver.initializeUsersSpace(lconn);
        }
    }

    public static void initVersioning(DataSource aSource) throws Exception {
        try (Connection lconn = aSource.getConnection()) {
            lconn.setAutoCommit(false);
            String dialect = dialectByConnection(lconn);
            SqlDriver driver = SQLUtils.getSqlDriver(dialect);
            driver.initializeVersion(lconn);
        }
    }
}
