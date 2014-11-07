/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.designer.application.query.result;

import com.bearsoft.rowset.RowsetConverter;
import com.bearsoft.rowset.changes.Change;
import com.bearsoft.rowset.events.RowsetAdapter;
import com.bearsoft.rowset.events.RowsetInsertEvent;
import com.bearsoft.rowset.exceptions.RowsetException;
import com.bearsoft.rowset.metadata.DataTypeInfo;
import com.bearsoft.rowset.metadata.Field;
import com.bearsoft.rowset.metadata.Fields;
import com.bearsoft.rowset.metadata.Parameter;
import com.bearsoft.rowset.metadata.Parameters;
import com.bearsoft.rowset.utils.IDGenerator;
import com.bearsoft.rowset.utils.RowsetUtils;
import com.eas.client.DatabasesClient;
import com.eas.client.SQLUtils;
import com.eas.client.SqlQuery;
import com.eas.client.StoredQueryFactory;
import com.eas.client.model.Relation;
import com.eas.client.model.application.ApplicationDbEntity;
import com.eas.client.model.application.ApplicationDbModel;
import com.eas.client.queries.LocalQueriesProxy;
import com.eas.client.queries.ScriptedQueryFactory;
import com.eas.dbcontrols.grid.DbGrid;
import com.eas.designer.application.indexer.IndexerQuery;
import com.eas.designer.application.query.PlatypusQueryDataObject;
import com.eas.designer.application.query.editing.SqlTextEditsComplementor;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.NamedParameter;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author vv
 */
public class QueryResultsView extends javax.swing.JPanel {

    private final PlatypusQueryDataObject queryDataObject;
    private QuerySetupView querySetupView;
    private DbGrid dbGrid;
    private final DatabasesClient basesProxy;
    private String queryText;
    private Parameters parameters;
    private ApplicationDbModel model;
    private ApplicationDbEntity queryEntity;
    private static int queryIndex;
    private static CCJSqlParserManager parserManager = new CCJSqlParserManager();
    private static final String DEFAULT_TEXT_COLOR_KEY = "textText"; //NOI18N
    private static final String VALUE_PREF_KEY = "value"; //NOI18N
    private static final Logger logger = Logger.getLogger(QueryResultsView.class.getName());
    private static final int[] pageSizes = {100, 200, 500, 1000};
    private PageSizeItem[] pageSizeItems;
    private int pageSize;
    private String datasourceName;
    private String queryName;
    private final RowsetConverter converter = new RowsetConverter();

    public QueryResultsView(PlatypusQueryDataObject aQueryDataObject) throws Exception {
        super();
        initComponents();
        initPageSizes();
        initCopyMessage();
        queryDataObject = aQueryDataObject;
        basesProxy = aQueryDataObject.getBasesProxy();
        datasourceName = queryDataObject.getDatasourceName();
        String storedQueryText = queryDataObject.getSqlTextDocument().getText(0, queryDataObject.getSqlTextDocument().getLength());
        String storedDialectQueryText = queryDataObject.getSqlFullTextDocument().getText(0, queryDataObject.getSqlFullTextDocument().getLength());
        StoredQueryFactory factory = new ScriptedQueryFactory(basesProxy, queryDataObject.getProject().getQueries(), queryDataObject.getProject().getIndexer());
        queryText = factory.compileSubqueries(storedDialectQueryText != null && !storedDialectQueryText.isEmpty() ? storedDialectQueryText : storedQueryText, queryDataObject.getModel());
        parameters = queryDataObject.getModel().getParameters();
        setName(queryDataObject.getName());
        resetMessage();
        setupButtons();
        queryName = IndexerQuery.file2AppElementId(aQueryDataObject.getPrimaryFile());
        if (queryName != null && !queryName.isEmpty()) {
            loadParametersValues();
        }
    }

    public QueryResultsView(DatabasesClient aBasesProxy, String aDatasourceName, String aSchemaName, String aTableName) throws Exception {
        initComponents();
        initPageSizes();
        initCopyMessage();
        queryDataObject = null;
        basesProxy = aBasesProxy;
        queryText = String.format(SQLUtils.TABLE_NAME_2_SQL, getTableName(aSchemaName, aTableName)); //NOI18N
        datasourceName = aDatasourceName;
        parameters = new Parameters();
        setName(aTableName);
        resetMessage();
        setupButtons();
    }

    public QueryResultsView(PlatypusQueryDataObject aQueryDataObject, String aQueryText) throws Exception {
        initComponents();
        initPageSizes();
        initCopyMessage();
        queryDataObject = aQueryDataObject;
        basesProxy = aQueryDataObject.getBasesProxy();
        queryText = aQueryText;
        parseParameters();
        setName(getGeneratedTitle());
        resetMessage();
        runButton.setEnabled(false);
        refreshButton.setEnabled(false);
        commitButton.setEnabled(false);
    }

    private void setupButtons() {
        runButton.setEnabled(false);
        refreshButton.setEnabled(false);
        commitButton.setEnabled(false);
        nextPageButton.setEnabled(false);
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public PageSizeItem[] getPageSizeItems() {
        return pageSizeItems;
    }

    private static String getTableName(String aTableSchemaName, String aTableName) {
        if (aTableName == null || aTableName.isEmpty()) {
            throw new IllegalArgumentException("Table name is null or empty."); //NOI18N
        }
        return aTableSchemaName != null && !aTableSchemaName.isEmpty() ? String.format("%s.%s", aTableSchemaName, aTableName) : aTableName; //NOI18N
    }

    /**
     *
     * @return True if underlying suery is a select, false if it a insert,
     * delete, update or stored procedure call
     * @throws Exception
     */
    private boolean initModel() throws Exception {
        model = new ApplicationDbModel(basesProxy, null);
        setupQueryEntityBySql();
        // enable dataworks
        if (queryEntity.getQuery().isCommand()) {
            queryEntity.getQuery().setManual(true);
            model.requery();
            int rowsAffected = basesProxy.executeUpdate(queryEntity.getQuery().compile(), null, null);
            showInfo(NbBundle.getMessage(QuerySetupView.class, "QueryResultsView.affectedRowsMessage", rowsAffected));
            return false;
        } else {
            model.requery();
            return true;
        }
    }

    private void setupQueryEntityBySql() throws Exception {
        if (queryEntity != null) {
            queryEntity.setModel(null);
            model.removeEntity(queryEntity);
        }
        queryEntity = model.newGenericEntity();
        queryEntity.setModel(model);
        SqlQuery query = new SqlQuery(basesProxy, queryText);
        query.setDbId(datasourceName);
        query.setPageSize(pageSize);
        parameters.toCollection().stream().forEach((p) -> {
            query.getParameters().add(p.copy());
        });
        queryEntity.setQuery(query);
        model.addEntity(queryEntity);
        try {
            StoredQueryFactory factory = new StoredQueryFactory(basesProxy, null, false);
            query.setCommand(!factory.putTableFieldsMetadata(query));
            enableCommitQueryButton(!query.isCommand());
            enableNextPageButton(!query.isCommand());
            enableAddButton(!query.isCommand());
            hintCommitQueryButton(NbBundle.getMessage(QueryResultsView.class, "HINT_Commit"));
        } catch (Exception ex) {
            query.setFields(null);// We have to accept database's fields here.
            enableCommitQueryButton(false);
            enableNextPageButton(false);
            enableAddButton(false);
            hintCommitQueryButton(NbBundle.getMessage(QueryResultsView.class, "HINT_Uncommitable"));
        }
        enableRefreshQueryButton(true);
        queryEntity.prepareRowsetByQuery();
    }

    public PlatypusQueryDataObject getQueryDataObject() {
        return queryDataObject;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String aQueryText) {
        queryText = aQueryText;
    }

    private String getGeneratedTitle() {
        return String.format("%s %d", NbBundle.getMessage(QuerySetupView.class, "QueryResultsView.queryName"), ++queryIndex); //NOI18N
    }

    private void resetMessage() {
        showInfo(""); // NOI18N
    }

    private void showInfo(final String aText) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                messageLabel.setForeground(UIManager.getColor(DEFAULT_TEXT_COLOR_KEY));
                messageLabel.setText(aText);
                messageLabel.setCaretPosition(0);
            }
        });
    }

    private void showWarning(final String aText) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                messageLabel.setForeground(Color.RED.darker());
                messageLabel.setText(aText);
                messageLabel.setCaretPosition(0);
            }
        });
    }

    private void showQueryResultsMessage() throws Exception {
        String message = String.format(NbBundle.getMessage(QuerySetupView.class, "QueryResultsView.resultMessage"), queryEntity.getRowset().size());
        List<Field> pks = queryEntity.getRowset().getFields().getPrimaryKeys();
        if (pks == null || pks.isEmpty()) {
            message += "\n " + String.format(NbBundle.getMessage(QuerySetupView.class, "QueryResultsView.noKeysMessage"), queryEntity.getEntityId());
        }
        showInfo(message);
    }

    public void logParameters() throws Exception {
        if (logger.isLoggable(Level.FINEST)) {
            for (int i = 1; i <= parameters.getParametersCount(); i++) {
                Parameter p = parameters.get(i);
                logger.log(Level.FINEST, "Parameter {0} of type {1} is assigned with value: {2}", new Object[]{p.getName(), p.getTypeInfo().getSqlTypeName(), p.getValue()});
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBar = new javax.swing.JToolBar();
        refreshButton = new javax.swing.JButton();
        nextPageButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        commitButton = new javax.swing.JButton();
        runButton = new javax.swing.JButton();
        verticalFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        resultsPanel = new javax.swing.JPanel();
        gridPanel = new javax.swing.JPanel();
        footerPanel = new javax.swing.JPanel();
        messageLabel = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        toolBar.setFloatable(false);
        toolBar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        toolBar.setRollover(true);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/eas/designer/application/query/result/refresh-records-btn.png"))); // NOI18N
        refreshButton.setText(org.openide.util.NbBundle.getMessage(QueryResultsView.class, "QueryResultsView.refreshButton.text")); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(QueryResultsView.class, "refreshButton.Tooltip")); // NOI18N
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        toolBar.add(refreshButton);

        nextPageButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/eas/designer/application/query/result/next.png"))); // NOI18N
        nextPageButton.setText(org.openide.util.NbBundle.getMessage(QueryResultsView.class, "QueryResultsView.nextPageButton.text")); // NOI18N
        nextPageButton.setToolTipText(org.openide.util.NbBundle.getMessage(QueryResultsView.class, "nextPageButton.Tooltip")); // NOI18N
        nextPageButton.setFocusable(false);
        nextPageButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextPageButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        nextPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPageButtonActionPerformed(evt);
            }
        });
        toolBar.add(nextPageButton);

        addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/eas/designer/application/query/result/new.png"))); // NOI18N
        addButton.setText(org.openide.util.NbBundle.getMessage(QueryResultsView.class, "QueryResultsView.addButton.text")); // NOI18N
        addButton.setToolTipText(org.openide.util.NbBundle.getMessage(QueryResultsView.class, "addButton.Tooltip")); // NOI18N
        addButton.setFocusable(false);
        addButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        toolBar.add(addButton);

        deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/eas/designer/application/query/result/delete.png"))); // NOI18N
        deleteButton.setText(org.openide.util.NbBundle.getMessage(QueryResultsView.class, "QueryResultsView.deleteButton.text")); // NOI18N
        deleteButton.setToolTipText(org.openide.util.NbBundle.getMessage(QueryResultsView.class, "deleteButton.Tooltip")); // NOI18N
        deleteButton.setFocusable(false);
        deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        toolBar.add(deleteButton);

        commitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/eas/designer/application/query/result/commit-record-btn.png"))); // NOI18N
        commitButton.setText(org.openide.util.NbBundle.getMessage(QueryResultsView.class, "QueryResultsView.commitButton.text")); // NOI18N
        commitButton.setFocusable(false);
        commitButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        commitButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        commitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commitButtonActionPerformed(evt);
            }
        });
        toolBar.add(commitButton);

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/eas/designer/application/query/result/runsql.png"))); // NOI18N
        runButton.setText(org.openide.util.NbBundle.getMessage(QueryResultsView.class, "QueryResultsView.runButton.text")); // NOI18N
        runButton.setToolTipText(org.openide.util.NbBundle.getMessage(QueryResultsView.class, "runButton.Tooltip")); // NOI18N
        runButton.setFocusable(false);
        runButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        runButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        toolBar.add(runButton);
        toolBar.add(verticalFiller);

        add(toolBar, java.awt.BorderLayout.WEST);

        resultsPanel.setLayout(new java.awt.BorderLayout());

        gridPanel.setLayout(new java.awt.BorderLayout());
        resultsPanel.add(gridPanel, java.awt.BorderLayout.CENTER);

        footerPanel.setPreferredSize(new java.awt.Dimension(10, 22));
        footerPanel.setLayout(new java.awt.BorderLayout());

        messageLabel.setEditable(false);
        messageLabel.setBorder(null);
        messageLabel.setOpaque(false);
        footerPanel.add(messageLabel, java.awt.BorderLayout.CENTER);

        resultsPanel.add(footerPanel, java.awt.BorderLayout.SOUTH);

        add(resultsPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        try {
            showQuerySetupDialog();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_runButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        if (model != null) {
            RequestProcessor.getDefault().execute(new Runnable() {
                @Override
                public void run() {
                    final ProgressHandle ph = ProgressHandleFactory.createHandle(getName());
                    ph.start();
                    try {
                        if (queryEntity.getQuery().isCommand()) {
                            int rowsAffected = basesProxy.executeUpdate(queryEntity.getQuery().compile(), null, null);
                            showInfo(NbBundle.getMessage(QuerySetupView.class, "QueryResultsView.affectedRowsMessage", rowsAffected));
                        } else {
                            model.requery();
                            showQueryResultsMessage();
                        }
                    } catch (Exception ex) {
                        showWarning(ex.getMessage()); //NO1I18N
                    } finally {
                        ph.finish();
                    }
                }
            });

        }
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void commitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commitButtonActionPerformed
        try {
            if (model != null && model.isModified()) {
                commitButton.setEnabled(false);
                final String entityName = IDGenerator.genID().toString();
                queryEntity.getQuery().setEntityId(entityName);
                queryEntity.setQueryName(entityName);
                model.forEachChange((Change aChange) -> {
                    aChange.entityName = entityName;
                });
                ((LocalQueriesProxy) basesProxy.getQueries()).putCachedQuery(entityName, (SqlQuery) queryEntity.getQuery());
                RequestProcessor.getDefault().execute(() -> {
                    final ProgressHandle ph = ProgressHandleFactory.createHandle(getName());
                    ph.start();
                    try {
                        model.save();
                        showWarning(NbBundle.getMessage(QueryResultsView.class, "DataSaved"));
                    } catch (Exception ex) {
                        model.revert();
                        showInfo(ex.getMessage()); //NO1I18N
                    } finally {
                        ph.finish();
                        EventQueue.invokeLater(() -> {
                            ((LocalQueriesProxy) basesProxy.getQueries()).clearCachedQuery(entityName);
                            model.forEachChange((Change aChange) -> {
                                aChange.entityName = entityName;
                            });
                            queryEntity.getQuery().setEntityId(entityName);
                            queryEntity.setQueryName(entityName);
                            commitButton.setEnabled(true);
                        });
                    }
                });
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_commitButtonActionPerformed

    private void nextPageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextPageButtonActionPerformed
        if (model != null && queryEntity != null) {
            try {
                queryEntity.getRowset().nextPage(null, null);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_nextPageButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        dbGrid.insertRow();
    }//GEN-LAST:event_addButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        dbGrid.deleteRow();
    }//GEN-LAST:event_deleteButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton commitButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JPanel gridPanel;
    private javax.swing.JTextField messageLabel;
    private javax.swing.JButton nextPageButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JButton runButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.Box.Filler verticalFiller;
    // End of variables declaration//GEN-END:variables

    public void runQuery() {
        assert parameters != null : "Parameters must be initialized."; //NOI18N
        if (!parameters.isEmpty()) {
            showQuerySetupDialog();
        } else {
            requestExecuteQuery();
        }
    }

    private void showQuerySetupDialog() {
        try {
            if (querySetupView == null) {
                querySetupView = new QuerySetupView(this);
            }
            DialogDescriptor nd = new DialogDescriptor(querySetupView, getName());
            Dialog dlg = DialogDisplayer.getDefault().createDialog(nd);
            dlg.setModal(true);
            querySetupView.setDialog(dlg, nd);
            dlg.setVisible(true);
            if (DialogDescriptor.OK_OPTION.equals(nd.getValue())) {
                queryText = querySetupView.getSqlText();
                parameters = querySetupView.getParameters().copy();
                logParameters();
                if (queryName != null && !queryName.isEmpty() && querySetupView.isSaveParamsValuesEnabled()) {
                    saveParametersValues();
                }
                requestExecuteQuery();
            } else {
                enableRunQueryButton(true);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void requestExecuteQuery() {
        RequestProcessor.getDefault().execute(new Runnable() {
            @Override
            public void run() {
                execute();
            }
        });
    }

    private void execute() {
        enableRunQueryButton(false);
        final ProgressHandle ph = ProgressHandleFactory.createHandle(getName());
        ph.start();
        resetMessage();
        try {
            if (refresh()) {
                showQueryResultsMessage();
                gridPanel.revalidate();
                gridPanel.repaint();
            }
        } catch (Throwable ex) {
            showWarning(ex.getMessage());
            enableCommitQueryButton(false);
            enableNextPageButton(false);
            enableDeleteButton(false);
            enableAddButton(false);
        } finally {
            enableRunQueryButton(true);
            ph.finish();
        }
    }

    private void enableRunQueryButton(final boolean enable) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                runButton.setEnabled(enable);
            }
        });
    }

    private void enableRefreshQueryButton(final boolean enable) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refreshButton.setEnabled(enable);
            }
        });
    }

    private void enableCommitQueryButton(final boolean enable) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                commitButton.setEnabled(enable);
            }
        });
    }

    private void hintCommitQueryButton(final String aHintText) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                commitButton.setToolTipText(aHintText);
            }
        });
    }

    private void enableNextPageButton(final boolean enable) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                nextPageButton.setEnabled(enable);
            }
        });
    }

    private void enableAddButton(final boolean enable) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                addButton.setEnabled(enable);
            }
        });
    }

    private void enableDeleteButton(final boolean enable) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                deleteButton.setEnabled(enable);
            }
        });
    }

    /**
     *
     * @return True if results grid is initialized and false if dml query has
     * been executed
     * @throws Exception
     */
    private boolean refresh() throws Exception {
        if (initModel()) {
            initDbGrid();
            return true;
        } else {
            return false;
        }
    }

    private void initDbGrid() throws Exception {
        gridPanel.removeAll();
        dbGrid = new DbGrid();
        dbGrid.setModel(model);
        gridPanel.add(dbGrid);
        DbGrid.fillByEntity(queryEntity, dbGrid, 120);
        List<Field> pks = queryEntity.getRowset().getFields().getPrimaryKeys();
        dbGrid.setEditable(pks != null && !pks.isEmpty());
        dbGrid.setDeletable(pks != null && !pks.isEmpty());
        deleteButton.setEnabled(pks != null && !pks.isEmpty());
        showInfo(String.format(NbBundle.getMessage(QuerySetupView.class, "QueryResultsView.noKeysMessage"), queryEntity.getEntityId()));
        queryEntity.getRowset().addRowsetListener(new RowsetAdapter() {
            @Override
            public boolean willInsertRow(RowsetInsertEvent event) {
                try {
                    int modified = 0;
                    Fields fields = event.getRow().getFields();
                    List<Field> pks = fields.getPrimaryKeys();
                    for (int i = 1; i <= fields.getFieldsCount(); i++) {
                        Field field = fields.get(i);
                        if (!field.isNullable()) {
                            Object oValue;
                            if (field.isFk() || pks.isEmpty()) {// ask a user about a fk-field value and all other fields if primary keys are absent
                                String sValue = askFieldValue(field);
                                if (sValue == null) {
                                    return false;
                                } else {
                                    oValue = sValue;
                                }
                            } else {
                                oValue = generateFieldValue(field);
                            }
                            oValue = event.getRowset().getConverter().convert2RowsetCompatible(oValue, field.getTypeInfo());
                            event.getRow().setColumnObject(i, oValue);
                            modified++;
                        }
                    }
                    if (modified == 0 && !fields.isEmpty()) {// ask a user about all fields
                        for (int i = 1; i <= fields.getFieldsCount(); i++) {
                            Field field = fields.get(i);
                            Object oValue;
                            String sValue = askFieldValue(field);
                            if (sValue == null) {
                                return false;
                            } else {
                                oValue = sValue;
                            }
                            oValue = event.getRowset().getConverter().convert2RowsetCompatible(oValue, field.getTypeInfo());
                            event.getRow().setColumnObject(i, oValue);
                        }
                    }
                } catch (RowsetException ex) {
                    ErrorManager.getDefault().notify(ex);
                    return false;
                }
                return true;
            }

            private Object generateFieldValue(Field field) {
                Object oValue = RowsetUtils.generatePkValueByType(field.getTypeInfo().getSqlType());
                if (!field.isPk()) {// constant value for primary keys are harmful, because of uniqueness
                    if (oValue instanceof String) {
                        oValue = "\n";
                    } else if (oValue instanceof Date) {
                        oValue = new Date(0);
                    } else if (oValue instanceof Number) {
                        oValue = 0;
                    }
                }
                return oValue;
            }

            private String askFieldValue(Field field) {
                NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(
                        field.isFk()
                                ? NbBundle.getMessage(QueryResultsView.class, "QueryResultsView.LBL_ForeignKeyValue")
                                : NbBundle.getMessage(QueryResultsView.class, "QueryResultsView.LBL_FieldValue"),
                        field.isFk()
                                ? String.format(NbBundle.getMessage(QueryResultsView.class, "QueryResultsView.LBL_CantDetermineRequiredForeignKeyValue"), field.getTableName(), field.getName())
                                : String.format(NbBundle.getMessage(QueryResultsView.class, "QueryResultsView.LBL_CantDetermineRequiredValue"), field.getTableName(), field.getName()));
                Object oAnswer = DialogDisplayer.getDefault().notify(input);
                String sAnswer = input.getInputText();
                if (oAnswer == NotifyDescriptor.OK_OPTION) {
                    return sAnswer;
                } else {
                    return null;
                }
            }
        });
    }

    public Parameters getParameters() {
        return parameters;
    }

    private void parseParameters() throws JSQLParserException {
        Statement statement = parserManager.parse(new StringReader(queryText));
        parameters = new Parameters();
        Set<NamedParameter> parsedParameters = SqlTextEditsComplementor.extractParameters(statement);
        for (NamedParameter parsedParameter : parsedParameters) {
            Parameter newParameter = new Parameter(parsedParameter.getName());
            newParameter.setMode(1);
            newParameter.setTypeInfo(DataTypeInfo.VARCHAR);
            newParameter.setValue("");
            parameters.add(newParameter);
        }
    }

    private void initPageSizes() {
        pageSizeItems = new PageSizeItem[pageSizes.length];
        for (int i = 0; i < pageSizeItems.length; i++) {
            pageSizeItems[i] = new PageSizeItem(pageSizes[i]);
        }
        pageSize = pageSizes[0];
    }

    private void initCopyMessage() {
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem copyItem = new JMenuItem(NbBundle.getMessage(QuerySetupView.class, "QueryResultsView.copyMessage")); //NOI18N
        copyItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringSelection stringSelection = new StringSelection(messageLabel.getText());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        });
        menu.add(copyItem);
        messageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void loadParametersValues() {
        Preferences modulePreferences = NbPreferences.forModule(QueryResultsView.class);
        Preferences paramsPreferences = modulePreferences.node(queryName);
        for (Field parameter : parameters.toCollection()) {
            Preferences paramPreferences = paramsPreferences.node(parameter.getName());
            try {
                Object val = converter.convert2RowsetCompatible(paramPreferences.get(VALUE_PREF_KEY, ""), parameter.getTypeInfo()); //NOI18N
                if (val != null) {
                    ((Parameter) parameter).setValue(val);
                }
            } catch (Exception ex) {
                //no-op
            }
        }
    }

    private void saveParametersValues() {
        Preferences modulePreferences = NbPreferences.forModule(QueryResultsView.class);
        Preferences paramsPreferences = modulePreferences.node(queryName);
        for (Field parameter : parameters.toCollection()) {
            try {
                Preferences paramPreferences = paramsPreferences.node(parameter.getName());
                String strVal = (String) converter.convert2RowsetCompatible(((Parameter) parameter).getValue(), DataTypeInfo.VARCHAR);
                if (strVal != null) {
                    paramPreferences.put(VALUE_PREF_KEY, strVal);
                } else {
                    paramPreferences.remove(VALUE_PREF_KEY);
                }
            } catch (Exception ex) {
                //no-op
            }
        }
    }

    public static class PageSizeItem {

        private Integer pageSize;

        public PageSizeItem(int aPageSize) {
            pageSize = aPageSize;
        }

        public int getValue() {
            return pageSize;
        }

        @Override
        public String toString() {
            return String.format(NbBundle.getMessage(QuerySetupView.class, "QueryResultsView.pageSizeStr"), pageSize); //NOI18N
        }
    }
}
