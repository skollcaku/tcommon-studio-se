// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.updates.runtime.engine.component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.service.ComponentsInstallComponent;
import org.talend.commons.runtime.service.PatchComponent;
import org.talend.commons.utils.resource.UpdatesHelper;
import org.talend.updates.runtime.nexus.component.ComponentIndexBean;
import org.talend.updates.runtime.nexus.component.ComponentIndexManager;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class LocalComponentsInstallComponent implements ComponentsInstallComponent {

    private boolean needRelaunch;

    private String installedMessage;

    private String failureMessage;

    private File userComponentFolder = null;

    private List<File> failedComponents;

    private boolean isLogin = false;

    private File tmpM2RepoFolder;

    @Override
    public void setLogin(boolean login) {
        this.isLogin = login;
    }

    @Override
    public boolean needRelaunch() {
        return needRelaunch;
    }

    @Override
    public List<File> getFailedComponents() {
        if (failedComponents == null) {
            failedComponents = new ArrayList<>();
        }
        return failedComponents;
    }

    private void reset() {
        needRelaunch = false;
        if (failedComponents != null) {
            failedComponents.clear();
        }
        failedComponents = new ArrayList<>();
        installedMessage = null;
        failureMessage = null;
    }

    @Override
    public void setComponentFolder(File componentFolder) {
        this.userComponentFolder = componentFolder;
    }

    protected File getUserComponentFolder() {
        if (userComponentFolder == null) {
            // use same folder of user component from preference setting.
            ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,
                    "org.talend.designer.codegen"); //$NON-NLS-1$
            // same key as IComponentPreferenceConstant.USER_COMPONENTS_FOLDER
            String userCompFolder = preferenceStore.getString("USER_COMPONENTS_FOLDER"); //$NON-NLS-1$
            if (!StringUtils.isEmpty(userCompFolder)) {
                File compFolder = new File(userCompFolder);
                if (compFolder.exists()) {
                    userComponentFolder = compFolder;
                }
            }

            if (userComponentFolder == null) {
                try {
                    userComponentFolder = new File(Platform.getInstallLocation().getDataArea(FOLDER_COMPS).getPath());
                } catch (IOException e) {
                    //
                }
            }
            if (userComponentFolder == null) {
                userComponentFolder = new File(System.getProperty("user.dir") + '/' + FOLDER_COMPS); //$NON-NLS-1$
            }
        }
        return userComponentFolder;
    }

    /**
     * same as PatchLocalInstallerManager.getInstallingPatchesFolder
     */
    protected File getPatchesFolder() {
        try {
            return new File(Platform.getInstallLocation().getDataArea(PatchComponent.FOLDER_PATCHES).getPath());
        } catch (IOException e) {
            //
        }
        return new File(System.getProperty("user.dir") + '/' + PatchComponent.FOLDER_PATCHES); //$NON-NLS-1$
    }

    public File getTempM2RepoFolder() {
        if (tmpM2RepoFolder == null) {
            tmpM2RepoFolder = new File(getPatchesFolder().getParentFile(), "m2temp"); //$NON-NLS-1$
        }
        return tmpM2RepoFolder;
    }

    /**
     * If have patches to be installed, will ask restart.
     */
    @Override
    public boolean install() {
        if (Platform.inDevelopmentMode()) { // for dev, no need install patches.
            return false;
        }
        return doInstall();
    }

    protected boolean doInstall() {
        reset();

        try {
            installFromFolder(getUserComponentFolder());
            if (isLogin) { // try to install the components from patches folder.
                // because in patches folder, will do after install user components.
                installFromFolder(getPatchesFolder());
            }
            return needRelaunch = failureMessage == null;
        } catch (Exception e) {
            if (!CommonsPlugin.isHeadless()) {
                // make sure to popup error dialog for studio
                ExceptionHandler.process(e);
            }
        }
        return false;
    }

    protected void installFromFolder(final File componentBaseFolder) {
        if (componentBaseFolder == null || !componentBaseFolder.exists() || !componentBaseFolder.isDirectory()) {
            return;
        }
        final ComponentIndexManager indexManager = new ComponentIndexManager();

        final File[] updateFiles = componentBaseFolder.listFiles(); // no children folders recursively.
        if (updateFiles != null && updateFiles.length > 0) {
            for (File f : updateFiles) {
                // must be file, and update site.
                if (f.isFile() && UpdatesHelper.isComponentUpdateSite(f)) {
                    try {
                        // get ComponentP2ExtraFeature from update site file
                        ComponentIndexBean indexBean = indexManager.create(f);
                        if (indexBean == null) {
                            getFailedComponents().add(f);
                            continue;
                        }
                        ComponentP2ExtraFeature feature = new ComponentP2ExtraFeature(indexBean);
                        feature.setLogin(isLogin);
                        NullProgressMonitor progressMonitor = new NullProgressMonitor();
                        if (feature.canBeInstalled(new NullProgressMonitor())) {
                            List<URI> repoUris = new ArrayList<>(1);
                            repoUris.add(URI.create("jar:" + f.toURI().toString() + "!/")); //$NON-NLS-1$//$NON-NLS-2$
                            analyzeInstalledStatus(feature.install(progressMonitor, repoUris));
                        }
                    } catch (Exception e) { // sometime, if reinstall it, will got one exception also.
                        // won't block others to install.
                        if (!CommonsPlugin.isHeadless()) {
                            ExceptionHandler.process(e);
                        }
                        getFailedComponents().add(f);
                    }
                }
            }
        }
    }

    public void analyzeInstalledStatus(IStatus status) {
        switch (status.getSeverity()) {
        case IStatus.ERROR:
        case IStatus.CANCEL:
            failureMessage = failureMessage.concat("\n").concat(status.getMessage()); //$NON-NLS-1$
            break;
        default:
            installedMessage = installedMessage.concat("\n").concat(status.getMessage()); //$NON-NLS-1$
            break;
        }
    }

    @Override
    public String getInstalledMessages() {
        return installedMessage;
    }

    @Override
    public String getFailureMessage() {
        return failureMessage;
    }

}
