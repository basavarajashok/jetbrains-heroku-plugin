package com.jetbrains.heroku.ui;

import com.heroku.api.Release;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.heroku.service.HerokuProjectService;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mh
 * @since 26.12.11
 */
public class HerokuReleasesWindow extends HerokuToolWindow {

    private AtomicInteger selectedRow;
    private ReleaseTableModel tableModel;

    @Override
    protected void setWindowInfo(ContentInfo contentInfo) {
        contentInfo.describe("Releases","/debugger/showCurrentFrame.png","Releases of this Application");
    }

    @Override
    protected JComponent createContentPane() {
        tableModel = new ReleaseTableModel(load());
        selectedRow = new AtomicInteger(-1);
        return GuiUtil.table(tableModel, selectedRow);
    }

    private List<Release> load() {
        if (!herokuProjectService.isHerokuProject()) return Collections.emptyList();

        return herokuProjectService.getReleases();
    }

    public void doUpdate() {
        setEnabled(herokuProjectService.isHerokuProject());
        tableModel.update(load());
    }

    @Override
    protected List<AnAction> createActions() {
        return Arrays.asList(
                new AnAction("Release Info", "", icon("/compiler/information.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final Release release = tableModel.getRelease(selectedRow.get());
                        if (release==null) return;
                        final Release releaseInfo = herokuProjectService.getReleaseInfo(release);
                        String html=tableModel.renderRelease(releaseInfo);
                        Messages.showMessageDialog(html, "Release Info", Messages.getInformationIcon());
                    }
                },
                new AnAction("Rollback", "", icon("/actions/rollback.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        final Release release = tableModel.getRelease(selectedRow.get());
                        if (release==null) return;
                        if (Messages.showYesNoDialog("Rollback to the Release: "+release.getName(),"Rollback to Release",Messages.getQuestionIcon())!=Messages.YES) return;
                        herokuProjectService.rollbackTo(release);
                        HerokuReleasesWindow.this.doUpdate();
                    }
                },
                new AnAction("Update", "", icon("/actions/sync.png")) {
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        HerokuReleasesWindow.this.doUpdate();
                    }
                }
        );
    }

    public HerokuReleasesWindow(HerokuProjectService herokuProjectService) {
        super(herokuProjectService);
    }
}
