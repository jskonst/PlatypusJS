/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.designer.explorer.project;

import com.eas.designer.application.indexer.IndexerQuery;
import com.eas.designer.application.project.PlatypusProject;
import com.eas.designer.explorer.FileChooser;
import java.util.HashSet;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author vv
 */
public class SelectAppElementPanel extends javax.swing.JPanel {

    private PlatypusProject project;
    
    public SelectAppElementPanel(PlatypusProject aProject) {
        project = aProject;
        initComponents();
    }
    
    public String getAppElementId() {
        return txtAppElement.getText();
    }

    public boolean isSaveAsDefault() {
        return cbSetAsDefault.isSelected();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnBrowse = new javax.swing.JButton();
        txtAppElement = new javax.swing.JTextField();
        lblAppElement = new javax.swing.JLabel();
        cbSetAsDefault = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(btnBrowse, org.openide.util.NbBundle.getMessage(SelectAppElementPanel.class, "SelectAppElementPanel.btnBrowse.text")); // NOI18N
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblAppElement, org.openide.util.NbBundle.getMessage(SelectAppElementPanel.class, "SelectAppElementPanel.lblAppElement.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbSetAsDefault, org.openide.util.NbBundle.getMessage(SelectAppElementPanel.class, "SelectAppElementPanel.cbSetAsDefault.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbSetAsDefault)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblAppElement)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAppElement, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAppElement, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAppElement)
                    .addComponent(btnBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSetAsDefault)
                .addContainerGap(21, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        try {
            Set<String> allowedTypes = new HashSet<>();
            allowedTypes.add("text/javascript");//NOI18N
            FileObject newSelectedFile = FileChooser.selectAppElement(project.getSrcRoot(), null, allowedTypes);
            if (newSelectedFile != null) {
                String appElementId = IndexerQuery.file2AppElementId(newSelectedFile);
                txtAppElement.setText(appElementId);
            } else {
                txtAppElement.setText("");//NOI18N
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_btnBrowseActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JCheckBox cbSetAsDefault;
    private javax.swing.JLabel lblAppElement;
    private javax.swing.JTextField txtAppElement;
    // End of variables declaration//GEN-END:variables
}
