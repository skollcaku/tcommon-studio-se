// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.updates.runtime.login;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.CommonsPlugin;
import org.talend.login.AbstractLoginTask;
import org.talend.updates.runtime.engine.component.ComponentNexusP2ExtraFeature;
import org.talend.updates.runtime.engine.component.InstallComponentMessages;
import org.talend.updates.runtime.engine.factory.ComponentsNexusInstallFactory;
import org.talend.updates.runtime.model.ExtraFeature;
import org.talend.updates.runtime.model.FeatureCategory;
import org.talend.updates.runtime.model.P2ExtraFeature;
import org.talend.updates.runtime.model.P2ExtraFeatureException;
import org.talend.updates.runtime.utils.OsgiBundleInstaller;

/**
 * 
 * DOC ggu class global comment. Detailled comment
 */
public class InstallLocalNexusComponentsLoginTask extends AbstractLoginTask {

    private static Logger log = Logger.getLogger(InstallLocalNexusComponentsLoginTask.class);

    class ComponentsLocalNexusInstallFactory extends ComponentsNexusInstallFactory {

        @Override
        protected Set<P2ExtraFeature> getAllExtraFeatures(IProgressMonitor monitor) {
            return getLocalNexusFeatures(monitor); // only get from local nexus
        }

    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 6, 7, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public boolean isCommandlineTask() {
        return true;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            ComponentsLocalNexusInstallFactory compInstallFactory = new ComponentsLocalNexusInstallFactory();

            Set<ExtraFeature> uninstalledExtraFeatures = new LinkedHashSet<ExtraFeature>();
            InstallComponentMessages messages = new InstallComponentMessages();

            compInstallFactory.retrieveUninstalledExtraFeatures(monitor, uninstalledExtraFeatures);
            for (ExtraFeature feature : uninstalledExtraFeatures) {
                install(monitor, feature, messages);
            }

            log.info(messages.getInstalledMessage());
            log.error(messages.getFailureMessage());

            if (!CommonsPlugin.isHeadless()) { //
                MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                        "Install components from Nexus", messages.getInstalledMessage());
            }
            if (messages.isOk() && !messages.isNeedRestart()) {
                OsgiBundleInstaller.reloadComponents();
            }
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    private void install(IProgressMonitor monitor, ExtraFeature feature, InstallComponentMessages messages)
            throws P2ExtraFeatureException {
        if (feature instanceof FeatureCategory) {
            Set<ExtraFeature> children = ((FeatureCategory) feature).getChildren();
            for (ExtraFeature f : children) {
                install(monitor, f, messages);
            }
        }
        if (feature instanceof ComponentNexusP2ExtraFeature) {
            ComponentNexusP2ExtraFeature compFeature = (ComponentNexusP2ExtraFeature) feature;
            if (compFeature.canBeInstalled(monitor)) {
                compFeature.needRestart();
                messages.analyzeStatus(compFeature.install(monitor));
                messages.setNeedRestart(compFeature.needRestart());
            }
        }
    }
}
