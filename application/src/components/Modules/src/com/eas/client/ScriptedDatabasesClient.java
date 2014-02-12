package com.eas.client;

import com.bearsoft.rowset.changes.Change;
import com.bearsoft.rowset.dataflow.FlowProvider;
import com.bearsoft.rowset.metadata.DataTypeInfo;
import com.bearsoft.rowset.metadata.Field;
import com.bearsoft.rowset.metadata.Fields;
import com.bearsoft.rowset.metadata.ForeignKeySpec;
import com.eas.client.cache.FilesAppCache;
import com.eas.client.cache.FreqCache;
import com.eas.client.exceptions.UnboundSqlParameterException;
import com.eas.client.metadata.ApplicationElement;
import com.eas.client.queries.PlatypusScriptedFlowProvider;
import com.eas.client.queries.SqlCompiledQuery;
import com.eas.client.queries.SqlQuery;
import com.eas.client.scripts.ScriptRunner;
import com.eas.script.ScriptUtils;
import com.eas.script.ScriptUtils.ScriptAction;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

/**
 * Multi data source client. It allows to use js modules as datasources,
 * validators and appliers.
 *
 * @author mg
 */
public class ScriptedDatabasesClient extends DatabasesClient {

    protected static final String JAVASCRIPT_QUERY_CONTENTS = "javascript query";
    // key - validator name, value datasources list
    protected Map<String, Collection<String>> validators = new HashMap<>();
    protected FreqCache<String, SqlQuery> scriptedQueries = new FreqCache<String, SqlQuery>() {
        @Override
        protected SqlQuery getNewEntry(final String aQueryId) throws Exception {
            ApplicationElement ae = appCache.get(aQueryId);
            if (ae != null && ae.getType() == ClientConstants.ET_COMPONENT) {
                return ScriptUtils.inContext(new ScriptAction() {
                    @Override
                    public SqlQuery run(Context cx) throws Exception {
                        final String aDatasourceId = aQueryId;
                        SqlQuery query = new SqlQuery(ScriptedDatabasesClient.this) {
                            @Override
                            public SqlCompiledQuery compile() throws UnboundSqlParameterException, Exception {
                                SqlCompiledQuery compiled = new SqlCompiledQuery(ScriptedDatabasesClient.this, aDatasourceId, aQueryId, JAVASCRIPT_QUERY_CONTENTS, getParameters(), getFields(), Collections.<String>emptySet(), Collections.<String>emptySet());
                                return compiled;
                            }

                            @Override
                            public boolean isPublicAccess() {
                                return true;
                            }
                        };
                        ScriptRunner schemaContainer = createModule(cx, aQueryId);
                        if (schemaContainer != null) {
                            Fields fields = new Fields();
                            query.setFields(fields);
                            query.setEntityId(aQueryId);
                            query.setDbId(aDatasourceId);
                            Object oSchema = schemaContainer.get("schema", schemaContainer);
                            readScriptFields(aQueryId, oSchema, fields);
                            for (Field p : ((ScriptRunner) schemaContainer).getModel().getParameters().toCollection()) {
                                query.putParameter(p.getName(), p.getTypeInfo(), null);
                            }
                            return query;
                        } else {
                            throw new IllegalStateException(" datasource module: " + aQueryId + " not found");
                        }
                    }

                    private void readScriptFields(String aQueryId, Object oSchema, Fields fields) {
                        if (oSchema instanceof Scriptable) {
                            Scriptable sSchema = (Scriptable) oSchema;
                            Object oLength = sSchema.get("length", sSchema);
                            if (oLength instanceof Number) {
                                int length = ((Number) oLength).intValue();
                                for (int i = 0; i < length; i++) {
                                    Object oElement = sSchema.get(i, sSchema);
                                    if (oElement instanceof Scriptable) {
                                        Scriptable sElement = (Scriptable) oElement;
                                        Object oName = sElement.get("name", sElement);
                                        String sName = oName != Scriptable.NOT_FOUND ? Context.toString(oName) : null;
                                        if (sName != null && !sName.isEmpty()) {
                                            Field field = new Field();
                                            field.setTypeInfo(DataTypeInfo.OTHER);
                                            fields.add(field);
                                            field.setName(sName);
                                            field.setOriginalName(sName);
                                            Object oEntity = sElement.get("entity", sElement);
                                            String sEntity = oEntity != Scriptable.NOT_FOUND ? Context.toString(oEntity) : null;
                                            if (sEntity != null && !sEntity.isEmpty()) {
                                                field.setTableName(sEntity);
                                            } else {
                                                field.setTableName(aQueryId);
                                            }
                                            Object oDescription = sElement.get("description", sElement);
                                            String sDescription = oDescription != Scriptable.NOT_FOUND ? Context.toString(oDescription) : null;
                                            if (sDescription != null && !sDescription.isEmpty()) {
                                                field.setDescription(sDescription);
                                            }
                                            Object oType = sElement.get("type", sElement);
                                            if (oType instanceof IdFunctionObject) {
                                                IdFunctionObject f = (IdFunctionObject) oType;
                                                if (String.class.getSimpleName().equals(f.getFunctionName())) {
                                                    field.setTypeInfo(DataTypeInfo.VARCHAR.copy());
                                                } else if (Number.class.getSimpleName().equals(f.getFunctionName())) {
                                                    field.setTypeInfo(DataTypeInfo.DECIMAL.copy());
                                                } else if (Boolean.class.getSimpleName().equals(f.getFunctionName())) {
                                                    field.setTypeInfo(DataTypeInfo.BOOLEAN.copy());
                                                } else if (Date.class.getSimpleName().equals(f.getFunctionName())) {
                                                    field.setTypeInfo(DataTypeInfo.TIMESTAMP.copy());
                                                }
                                            }
                                            Object oRequired = sElement.get("required", sElement);
                                            if (oRequired != Scriptable.NOT_FOUND) {
                                                boolean bRequired = Context.toBoolean(oRequired);
                                                field.setNullable(!bRequired);
                                            }
                                            Object oKey = sElement.get("key", sElement);
                                            if (oKey != Scriptable.NOT_FOUND) {
                                                boolean bKey = Context.toBoolean(oKey);
                                                field.setPk(bKey);
                                                field.setNullable(false);
                                            }
                                            Object oRef = sElement.get("ref", sElement);
                                            if (oRef != Scriptable.NOT_FOUND && oRef instanceof Scriptable) {
                                                Scriptable sRef = (Scriptable) oRef;
                                                Object oProperty = sRef.get("property", sRef);
                                                if (oProperty != Scriptable.NOT_FOUND) {
                                                    String sProperty = Context.toString(oProperty);
                                                    if (sProperty != null && !sProperty.isEmpty()) {
                                                        Object oRefEntity = sRef.get("entity", sRef);
                                                        String sRefEntity;
                                                        if (oRefEntity == Scriptable.NOT_FOUND) {
                                                            sRefEntity = aQueryId;
                                                        } else {
                                                            sRefEntity = Context.toString(oRefEntity);
                                                            if (sRefEntity == null || sRefEntity.isEmpty()) {
                                                                sRefEntity = aQueryId;
                                                            }
                                                        }
                                                        field.setFk(new ForeignKeySpec(null, aQueryId, field.getName(), null, ForeignKeySpec.ForeignKeyRule.CASCADE, ForeignKeySpec.ForeignKeyRule.CASCADE, false, null, sRefEntity, sProperty, null));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            } else {
                return null;
            }
        }
    };

    /**
     * @inheritDoc
     */
    public ScriptedDatabasesClient(AppCache anAppCache, String aDefaultDatasourceName, boolean aAutoFillMetadata) throws Exception {
        super(anAppCache, aDefaultDatasourceName, aAutoFillMetadata);
    }


    /**
     * Adds transaction validator module. Validator modules are used in commit
     * to verify transaction changes log. They mey consume particuled changes
     * and optionally send them to custom data store or a service. If validator
     * module detects an errorneous data changes, than it should thor ab
     * exception.
     *
     * @param aModuleId
     */
    public void addValidator(String aModuleId, Collection<String> aDatasources) {
        validators.put(aModuleId, aDatasources);
    }

    @Override
    public void clearQueries() throws Exception {
        super.clearQueries();
        scriptedQueries.clear();
    }

    @Override
    public void appEntityChanged(String aEntityId) throws Exception {
        super.appEntityChanged(aEntityId);
        if (scriptedQueries.containsKey(aEntityId)) {
            clearQueries();
        }
    }

    @Override
    public SqlQuery getAppQuery(final String aQueryId, boolean aCopy) throws Exception {
        SqlQuery query = scriptedQueries.get(aQueryId);
        if (query == null) {
            query = super.getAppQuery(aQueryId, aCopy);
        }
        return query;
    }

    @Override
    public synchronized DbMetadataCache getDbMetadataCache(String aDatasourceId) throws Exception {
        ApplicationElement appElement = aDatasourceId != null ? getAppCache().get(aDatasourceId) : null;
        if (appElement == null || appElement.getType() != ClientConstants.ET_COMPONENT) {
            return super.getDbMetadataCache(aDatasourceId);
        } else {
            return null;
        }
    }

    protected ScriptRunner createModule(Context cx, String aModuleName) throws Exception {
        Scriptable scope = ScriptRunner.checkStandardObjects(cx);
        Object oConstructor = ScriptRuntime.name(cx, scope, aModuleName);
        if (oConstructor instanceof Function) {
            Function fConstructor = (Function) oConstructor;
            Scriptable created = fConstructor.construct(cx, scope, new Object[]{});
            assert created instanceof ScriptRunner : "Datasource or validator class must be a platypus module";
            return (ScriptRunner) created;
        } else {
            return null;
        }
    }

    @Override
    public FlowProvider createFlowProvider(String aDbId, final String aSessionId, final String aEntityId, String aSqlClause, final Fields aExpectedFields, Set<String> aReadRoles, Set<String> aWriteRoles) throws Exception {
        if (JAVASCRIPT_QUERY_CONTENTS.equals(aSqlClause)) {
            return ScriptUtils.inContext(new ScriptAction() {
                @Override
                public FlowProvider run(Context cx) throws Exception {
                    ScriptRunner dataFeeder = createModule(cx, aEntityId);
                    if (dataFeeder != null) {
                        return new PlatypusScriptedFlowProvider(ScriptedDatabasesClient.this, aExpectedFields, (ScriptRunner) dataFeeder, aSessionId);
                    } else {
                        throw new IllegalStateException(" datasource module: " + aEntityId + " not found");
                    }
                }
            });
        } else {
            return super.createFlowProvider(aDbId, aSessionId, aEntityId, aSqlClause, aExpectedFields, aReadRoles, aWriteRoles);
        }
    }

    @Override
    protected int commit(final String aSessionId, final String aDatasourceId, final List<Change> aLog) throws Exception {
        ScriptUtils.inContext(new ScriptAction() {
            @Override
            public Object run(Context cx) throws Exception {
                for (String validatorName : validators.keySet()) {
                    Collection<String> datasourcesUnderControl = validators.get(validatorName);
                    // aDatasourceId must be null or it must be contained in datasourcesUnderControl to be validated by script validator
                    if (((datasourcesUnderControl == null || datasourcesUnderControl.isEmpty()) && aDatasourceId == null) || (datasourcesUnderControl != null && datasourcesUnderControl.contains(aDatasourceId))) {
                        ScriptRunner validator = createModule(cx, validatorName);
                        if (validator != null) {
                            Object oValidate = validator.get("validate", validator);
                            if (oValidate instanceof Function) {
                                Function fValidate = (Function) oValidate;
                                Object oResult = fValidate.call(cx, validator.getParentScope(), validator, new Object[]{Context.javaToJS(aLog.toArray(), validator.getParentScope()), aDatasourceId, aSessionId});
                                if (oResult != null && oResult != Context.getUndefinedValue() && Boolean.FALSE.equals(Context.toBoolean(oResult))) {
                                    break;
                                }
                            } else {
                                Logger.getLogger(ScriptedDatabasesClient.class.getName()).log(Level.WARNING, "\"validate\" method couldn''t be found in {0} module.", validatorName);
                            }
                        } else {
                            Logger.getLogger(ScriptedDatabasesClient.class.getName()).log(Level.WARNING, "{0} constructor couldn''t be found", validatorName);
                        }
                    }
                }
                if (aDatasourceId != null) {
                    ApplicationElement appElement = getAppCache().get(aDatasourceId);
                    if (appElement != null && appElement.getType() == ClientConstants.ET_COMPONENT) {
                        ScriptRunner dataSourceApplier = createModule(cx, aDatasourceId);
                        if (dataSourceApplier != null) {
                            Object oApply = dataSourceApplier.get("apply", dataSourceApplier);
                            if (oApply instanceof Function) {
                                Function fApply = (Function) oApply;
                                fApply.call(cx, dataSourceApplier.getParentScope(), dataSourceApplier, new Object[]{Context.javaToJS(aLog.toArray(), dataSourceApplier.getParentScope()), aSessionId});
                            }
                        }
                    }
                }
                return null;
            }
        });
        boolean consumed = true;
        for (Change change : aLog) {
            if (!change.consumed) {
                consumed = false;
            }
        }
        if (!consumed) {
            return super.commit(aSessionId, aDatasourceId, aLog);
        } else {
            aLog.clear();
            return 0;
        }
    }
}
