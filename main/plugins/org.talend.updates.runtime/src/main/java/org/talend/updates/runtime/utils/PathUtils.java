// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.updates.runtime.utils;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;

/**
 * created by ycbai on 2017年5月23日 Detailled comment
 *
 */
public class PathUtils {

    public static final String FOLDER_COMPS = "components"; //$NON-NLS-1$

    public static final String FOLDER_INSTALLED = "installed"; //$NON-NLS-1$

    public static final String FOLDER_SHARED = "shared"; //$NON-NLS-1$

    public static File getComponentsFolder() throws IOException {
        File componentsFolder = new File(Platform.getConfigurationLocation().getDataArea(FOLDER_COMPS).getPath());
        if (!componentsFolder.exists()) {
            componentsFolder.mkdirs();
        }
        return componentsFolder;
    }

    public static File getComponentsInstalledFolder() throws IOException {
        File installedComponentFolder = new File(getComponentsFolder(), FOLDER_INSTALLED);
        if (!installedComponentFolder.exists()) {
            installedComponentFolder.mkdirs();
        }
        return installedComponentFolder;
    }

    public static File getComponentsSharedFolder() throws IOException {
        File installedComponentFolder = new File(getComponentsFolder(), FOLDER_SHARED);
        if (!installedComponentFolder.exists()) {
            installedComponentFolder.mkdirs();
        }
        return installedComponentFolder;
    }

}
