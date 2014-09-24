/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.model.gui.selectors;

import com.eas.client.DatabasesClient;
import com.eas.designer.application.PlatypusUtils;
import com.eas.designer.application.utils.DatabaseConnections;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 *
 * @author mg
 */
public class DbSchemasComboModel implements ComboBoxModel<String> {

    protected DatabasesClient basesProxy;
    protected String dbId;
    protected String[] schemas = new String[]{};
    protected DbTablesListModel tablesModel;
    protected Set<ListDataListener> listeners = new HashSet<>();

    public DbSchemasComboModel(DatabasesClient aBasesProxy, String aDatasourceName, DbTablesListModel aTablesModel) {
        super();
        basesProxy = aBasesProxy;
        dbId = aDatasourceName;
        tablesModel = aTablesModel;
        achieveSchemas();
    }

    protected final void achieveSchemas() {
        try {
            DatabaseConnection conn = DatabaseConnections.lookup(dbId);
            if (conn != null && conn.getJDBCConnection() != null) {
                List<String> lschemas = PlatypusUtils.achieveSchemas(conn.getDatabaseURL(), conn.getUser(), conn.getPassword());
                if (lschemas != null) {
                    schemas = lschemas.toArray(new String[]{});
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(DbSchemasComboModel.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem instanceof String) {
            try {
                tablesModel.setSchema((String) anItem);
            } catch (Exception ex) {
                Logger.getLogger(DbSchemasComboModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public Object getSelectedItem() {
        return tablesModel.getSchema();
    }

    @Override
    public int getSize() {
        return schemas.length;
    }

    @Override
    public String getElementAt(int index) {
        if (index >= 0 && index < schemas.length) {
            return schemas[index];
        } else {
            return null;
        }
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }
}
