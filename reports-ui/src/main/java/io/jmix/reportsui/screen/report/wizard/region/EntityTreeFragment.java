package io.jmix.reportsui.screen.report.wizard.region;

import io.jmix.core.DataManager;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("report_EntityTree.fragment")
@UiDescriptor("entity-tree-frame.xml")
public class EntityTreeFragment extends ScreenFragment {

    @Autowired
    protected DataManager dataManager;
    @Autowired
    private CollectionContainer<EntityTreeNode> reportEntityTreeNodeDc;

    @Subscribe(target = Target.PARENT_CONTROLLER)
    public void onBeforeShow(Screen.BeforeShowEvent event) {
        RegionEditor regionEditor = (RegionEditor) getHostScreen();

        reportEntityTreeNodeDc.getMutableItems().add(regionEditor.getRootNode());
        reportEntityTreeNodeDc.getMutableItems().addAll(regionEditor.getRootNode().getChildren());
    }

}
