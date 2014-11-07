/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.designer.explorer.model.windows;

import com.bearsoft.rowset.metadata.Field;
import com.bearsoft.rowset.metadata.Parameter;
import com.eas.client.SqlQuery;
import com.eas.client.model.Entity;
import com.eas.client.model.Relation;
import com.eas.client.model.gui.selectors.SelectedField;
import com.eas.client.model.gui.selectors.SelectedParameter;
import com.eas.client.model.gui.view.ModelSelectionListener;
import com.eas.client.model.gui.view.model.ModelView;
import com.eas.designer.datamodel.nodes.EntityNode;
import com.eas.designer.datamodel.nodes.FieldNode;
import com.eas.designer.datamodel.nodes.ModelNode;
import com.eas.designer.datamodel.nodes.ShowEntityAction;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ClipboardEvent;
import org.openide.util.datatransfer.ClipboardListener;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//com.eas.designer.application.module.windows//ModelInspector//EN", autostore = false)
public final class ModelInspector extends TopComponent implements ExplorerManager.Provider {

    private static ModelInspector instance;
    /**
     * path to the icon used by the component and its open action
     */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "ModelInspector";

    public ModelInspector() {
        super();
        setName(NbBundle.getMessage(ModelInspector.class, "CTL_ModelInspector"));
        setToolTipText(NbBundle.getMessage(ModelInspector.class, "HINT_ModelInspector"));
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        explorerManager = new ExplorerManager();
        associateLookup(ExplorerUtils.createLookup(explorerManager, setupActionMap(getActionMap())));
        emptyInspectorNode = new EmptyInspectorNode();
        explorerManager.setRootContext(emptyInspectorNode);
        recreateComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300, Short.MAX_VALUE));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized ModelInspector getInstance() {
        if (instance == null) {
            instance = new ModelInspector();
        }
        return instance;
    }

    public static synchronized boolean isInstance() {
        return instance != null;
    }

    /**
     * Obtain the ModelInspector instance. Never call {@link #getDefault}
     * directly!
     */
    public static synchronized ModelInspector findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ModelInspector.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getInstance();
        }
        if (win instanceof ModelInspector) {
            return (ModelInspector) win;
        }
        Logger.getLogger(ModelInspector.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getInstance();
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
    // node for empty ComponentInspector

    private static class EmptyInspectorNode extends AbstractNode {

        public EmptyInspectorNode() {
            super(Children.LEAF);
        }

        @Override
        public boolean canRename() {
            return false;
        }
    }

    // listener on clipboard changes
    private class ClipboardChangesListener implements ClipboardListener {

        @Override
        public void clipboardChanged(ClipboardEvent ev) {
            if (!ev.isConsumed()) {
                updatePasteAction();
            }
        }
    }

    // performer for DeleteAction
    private class DeleteActionPerformer extends javax.swing.AbstractAction {

        @Override
        public boolean isEnabled() {
            Set<Class> nodesClasses = new HashSet<>();
            Node[] selected = getSelectedNodes();
            if (selected == null) {
                return false;
            }
            for (int i = 0; i < selected.length; i++) {
                nodesClasses.add(selected[i].getClass());
                if (!selected[i].canDestroy()) {
                    return false;
                }
            }
            return nodesClasses.size() == 1;// allow to destoroy only nodes of same type
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isEnabled()) {
                Node[] selected = getSelectedNodes();
                boolean nodesDeleted = false;
                for (int i = 0; i < selected.length; i++) {
                    if (selected[i].canDestroy()) {
                        try {
                            selected[i].destroy();
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                        nodesDeleted = true;
                    }
                }
                if (nodesDeleted) {
                    return;
                }
                try { // clear nodes selection first
                    getExplorerManager().setSelectedNodes(new Node[0]);
                } catch (PropertyVetoException ex) {
                } // cannot be vetoed

                Action toExecute = viewData.getModelView().getActionMap().get(ModelView.Delete.class.getSimpleName());
                if (toExecute.isEnabled()) {
                    toExecute.actionPerformed(new ActionEvent(this, 0, null));
                }
            }
        }
    }

    private class PasteActionPerformer extends javax.swing.AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            Action toExecute = viewData.getModelView().getActionMap().get(ModelView.Paste.class.getSimpleName());
            if (toExecute.isEnabled()) {
                toExecute.actionPerformed(new ActionEvent(this, 0, null));
            }
        }
    }

    // performer for CopyAction and CutAction
    private class CopyCutActionPerformer extends javax.swing.AbstractAction {

        private boolean copy;

        public CopyCutActionPerformer(boolean aCopy) {
            super();
            copy = aCopy;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Action toExecute;
            if (copy) {
                toExecute = viewData.getModelView().getActionMap().get(ModelView.Copy.class.getSimpleName());
            } else {
                toExecute = viewData.getModelView().getActionMap().get(ModelView.Cut.class.getSimpleName());
            }
            assert toExecute != null;
            if (toExecute.isEnabled()) {
                toExecute.actionPerformed(e);
            }
        }
    }
    private final transient ExplorerManager explorerManager;
    private final transient EmptyInspectorNode emptyInspectorNode;
    private final transient CopyCutActionPerformer copyActionPerformer = new CopyCutActionPerformer(true);
    private final transient CopyCutActionPerformer cutActionPerformer = new CopyCutActionPerformer(false);
    private final transient DeleteActionPerformer deleteActionPerformer = new DeleteActionPerformer();
    private final transient PasteActionPerformer pasteActionPerformer = new PasteActionPerformer();
    private transient ClipboardListener clipboardListener;
    private transient PropertyChangeListener nodesReflector;
    private transient ViewData<?, ?> viewData;

    public ViewData<?, ?> getViewData() {
        return viewData;
    }

    public <E extends Entity<?, SqlQuery, E>> void setViewData(ViewData<E, ?> aViewData) {
        if (viewData != aViewData) {
            if (viewData != null) {
                viewData.setNodesSelector(null);
            }
            viewData = aViewData;
            if (aViewData != null) {
                aViewData.setNodesSelector(new NodesSelector<>(nodesReflector, aViewData.getRootNode()));
            }
            if (viewData == null) {
                // swing memory leak work-around
                removeAll();
                recreateComponents();
                revalidate();

                getExplorerManager().setRootContext(emptyInspectorNode);
                try {
                    getExplorerManager().setSelectedNodes(new Node[]{});
                } catch (PropertyVetoException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                setActivatedNodes(new Node[]{});
            } else {
                // swing memory leak workaround
                removeAll();
                recreateComponents();
                revalidate();
                if (viewData.getRootNode() == null) { // model not loaded yet, should not happen
                    System.err.println("Warning: ModelExplorer.getRootNode() returns null"); // NOI18N
                    getExplorerManager().setRootContext(emptyInspectorNode);
                } else {
                    getExplorerManager().setRootContext(viewData.getRootNode());
                }
                try {
                    getExplorerManager().setSelectedNodes(viewData.getSelectedNodes());
                } catch (PropertyVetoException ex) {
                    ErrorManager.getDefault().notify(ex);   // should not happen
                }
                Node[] selected = getExplorerManager().getSelectedNodes();
                if (selected != null && selected.length > 0) {
                    setActivatedNodes(selected);
                } else {
                    setActivatedNodes(new Node[]{viewData.getRootNode()});
                }
            }
        }
    }

    public void setNodesReflector(PropertyChangeListener aNodesReflector) {
        if (nodesReflector != aNodesReflector) {
            if (nodesReflector != null) {
                getExplorerManager().removePropertyChangeListener(nodesReflector);
            }
            nodesReflector = aNodesReflector;
            if (nodesReflector != null) {
                getExplorerManager().addPropertyChangeListener(nodesReflector);
            }
        }
    }

    public class NodesSelector<E extends Entity<?, SqlQuery, E>> implements ModelSelectionListener<E> {

        protected transient PropertyChangeListener nodesReflector;
        protected transient ModelNode<E, ?> currentRootNode;

        public NodesSelector(PropertyChangeListener aNodesReflector, ModelNode<E, ?> aRootNode) {
            super();
            nodesReflector = aNodesReflector;
            currentRootNode = aRootNode;
        }

        @Override
        public void selectionChanged(Set<E> oldSelected, Set<E> newSelected) {
            getExplorerManager().removePropertyChangeListener(nodesReflector);
            try {
                Node[] oldNodes = getExplorerManager().getSelectedNodes();
                Node[] newNodes = convertSelectedToNodes(currentRootNode, oldNodes, oldSelected, newSelected);
                getExplorerManager().setSelectedNodes(newNodes);
                setActivatedNodes(getExplorerManager().getSelectedNodes());
            } catch (PropertyVetoException ex) {
                ErrorManager.getDefault().notify(ex);
            } finally {
                getExplorerManager().addPropertyChangeListener(nodesReflector);
            }
        }

        @Override
        public void selectionChanged(List<SelectedParameter<E>> aParameters, List<SelectedField<E>> aFields) {
            if (aParameters != null || aFields != null) {
                getExplorerManager().removePropertyChangeListener(nodesReflector);
                Node[] oldNodes = getExplorerManager().getSelectedNodes();
                try {
                    Node[] newNodes = convertSelectedToNodes(currentRootNode, oldNodes, aParameters, aFields);
                    getExplorerManager().setSelectedNodes(newNodes);
                    setActivatedNodes(getExplorerManager().getSelectedNodes());
                } catch (PropertyVetoException ex) {
                    ErrorManager.getDefault().notify(ex);
                } finally {
                    getExplorerManager().addPropertyChangeListener(nodesReflector);
                }
            }
        }

        @Override
        public void selectionChanged(Collection<Relation<E>> oldRelations, Collection<Relation<E>> newRelations) {
        }
    }

    public static <E extends Entity<?, SqlQuery, E>> Node[] convertSelectedToNodes(ModelNode<E, ?> aRootNode, Node[] oldNodes, Set<E> oldSelected, Set<E> newSelected) {
        List<Node> nodesToSelect = new ArrayList<>();
        nodesToSelect.addAll(Arrays.asList(aRootNode.entitiesToNodes(newSelected)));
        // changes were made to entities selection, and so no changes to fields selection should happen
        if (oldNodes != null) {
            for (Node n : oldNodes) {
                if (!(n instanceof EntityNode<?>)) {
                    nodesToSelect.add(n);
                }
            }
        }
        return nodesToSelect.toArray(new Node[]{});
    }

    public static <E extends Entity<?, SqlQuery, E>> Node[] convertSelectedToNodes(ModelNode<E, ?> aRootNode, Node[] oldNodes, List<SelectedParameter<E>> aParameters, List<SelectedField<E>> aFields) {
        List<Node> nodesToSelect = new ArrayList<>();
        if (aParameters != null) {
            for (SelectedParameter<E> sp : aParameters) {
                Set<E> entities = new HashSet<>();
                entities.add(sp.entity);
                Node[] entityNodes = aRootNode.entitiesToNodes(entities);
                if (entityNodes != null && entityNodes.length == 1) {
                    Set<Parameter> parameters = new HashSet<>();
                    parameters.add(sp.parameter);
                    Node[] paramNodes = ((EntityNode<E>) entityNodes[0]).fieldsToNodes(parameters);
                    nodesToSelect.addAll(Arrays.asList(paramNodes));
                }
            }
        }
        if (aFields != null) {
            for (SelectedField<E> sf : aFields) {
                Set<E> entities = new HashSet<>();
                entities.add(sf.entity);
                Node[] entityNodes = aRootNode.entitiesToNodes(entities);
                if (entityNodes != null && entityNodes.length == 1) {
                    Set<Field> fields = new HashSet<>();
                    fields.add(sf.field);
                    Node[] paramNodes = ((EntityNode<E>) entityNodes[0]).fieldsToNodes(fields);
                    nodesToSelect.addAll(Arrays.asList(paramNodes));
                }
            }
        }
        // changes were made to fields selection, and so, no changes to entities selection should happen
        if (oldNodes != null) {
            for (Node n : oldNodes) {
                if (n != null && !(n instanceof FieldNode)) {
                    nodesToSelect.add(n);
                }
            }
        }
        return nodesToSelect.toArray(new Node[]{});
    }

    public static class ViewData<E extends Entity<?, SqlQuery, E>, MV extends ModelView<E, ?>> {

        // current view data
        protected transient MV modelView;
        protected transient UndoRedo undoRedo;
        protected transient ModelNode<E, ?> rootNode;
        protected transient NodesSelector<E> nodesSelector;
        protected transient ShowEntityActionPerformer<E, MV> showEntityActionPerformer;

        public ViewData(MV aModelView, UndoRedo aUndoRedo, ModelNode<E, ?> aRootNode) {
            super();
            modelView = aModelView;
            undoRedo = aUndoRedo;
            rootNode = aRootNode;
        }

        public ModelNode<E, ?> getRootNode() {
            return rootNode;
        }

        public MV getModelView() {
            return modelView;
        }

        public UndoRedo getUndoRedo() {
            return undoRedo;
        }

        public ShowEntityActionPerformer<E, MV> getShowEntityActionPerformer() {
            if (showEntityActionPerformer == null) {
                ShowEntityActionPerformer<E, MV> seActionPerformer = new ShowEntityActionPerformer<>(modelView);
                showEntityActionPerformer = seActionPerformer;
            }
            return showEntityActionPerformer;
        }

        public NodesSelector<E> getNodesSelector() {
            return nodesSelector;
        }

        public void setNodesSelector(NodesSelector<E> aNodesSelector) {
            if (modelView != null && nodesSelector != aNodesSelector) {
                if (nodesSelector != null) {
                    modelView.removeModelSelectionListener(nodesSelector);
                }
                nodesSelector = aNodesSelector;
                if (nodesSelector != null) {
                    modelView.addModelSelectionListener(nodesSelector);
                }
            }
        }

        private Node[] getSelectedNodes() {
            return rootNode != null && modelView != null ? rootNode.entitiesToNodes(modelView.getSelectedEntities()) : new Node[]{};
        }
    }

    private void recreateComponents() {
        setLayout(new java.awt.BorderLayout());
        if (viewData != null && viewData.getRootNode() != null) {
            TreeView treeView = new BeanTreeView();
            treeView.setRootVisible(viewData.getRootNode().isVisibleRoot());
            treeView.setDragSource(true);
            treeView.setDropTarget(true);
            treeView.getAccessibleContext().setAccessibleName(
                    NbBundle.getMessage(ModelInspector.class, "CTL_DatasourcesList")); // NOI18N
            treeView.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(ModelInspector.class, "HINT_DatasourcesList")); // NOI18N
            add(java.awt.BorderLayout.CENTER, treeView);
        }
    }

    javax.swing.ActionMap setupActionMap(javax.swing.ActionMap map) {
        map.put(SystemAction.get(ShowEntityAction.class).getActionMapKey(), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (viewData != null && viewData.getShowEntityActionPerformer() != null) {
                    viewData.getShowEntityActionPerformer().actionPerformed(e);
                }
            }
        });
        map.put(SystemAction.get(CopyAction.class).getActionMapKey(), copyActionPerformer);
        map.put(SystemAction.get(CutAction.class).getActionMapKey(), cutActionPerformer);
        map.put(SystemAction.get(DeleteAction.class).getActionMapKey(), deleteActionPerformer); // NOI18N
        map.put(SystemAction.get(PasteAction.class).getActionMapKey(), pasteActionPerformer);
        return map;
    }

    /**
     * Overriden to explicitely set persistence type of ModelInspector to
     * PERSISTENCE_ALWAYS
     */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    private void updatePasteAction() {
        if (java.awt.EventQueue.isDispatchThread()) {
            updatePasteActionInAwtThread();
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updatePasteActionInAwtThread();
                }
            });
        }
    }

    private void updatePasteActionInAwtThread() {
        // pasting considered only on the first selected node
        Clipboard clipboard = getClipboard();
        Transferable trans = clipboard.getContents(this); // [this??]
        SystemAction.get(PasteAction.class).setEnabled(trans != null && trans.isDataFlavorSupported(DataFlavor.stringFlavor));
    }

    private Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(java.awt.datatransfer.Clipboard.class);
        if (c == null) {
            c = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return c;
    }

    public Node[] getSelectedNodes() {
        return getExplorerManager().getSelectedNodes();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public UndoRedo getUndoRedo() {
        return viewData != null && viewData.getUndoRedo() != null ? viewData.getUndoRedo() : super.getUndoRedo();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.component-inspector"); // NOI18N
    }

    /**
     * Replaces this in object stream.
     *
     * @return ResolvableHelper
     */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected void componentActivated() {
        attachActions();
    }

    @Override
    protected void componentDeactivated() {
        detachActions();
    }

    // ------------
    // activating and focusing
    synchronized void attachActions() {
        ExplorerUtils.activateActions(explorerManager, true);
        updatePasteAction();
        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            if (clipboardListener == null) {
                clipboardListener = new ClipboardChangesListener();
            }
            clip.addClipboardListener(clipboardListener);
        }
    }

    synchronized void detachActions() {
        ExplorerUtils.activateActions(explorerManager, false);
        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            clip.removeClipboardListener(clipboardListener);
        }
    }

    final public static class ResolvableHelper implements java.io.Serializable {

        static final long serialVersionUID = 7424646018839457544L;

        public Object readResolve() {
            return ModelInspector.getInstance();
        }
    }
}
