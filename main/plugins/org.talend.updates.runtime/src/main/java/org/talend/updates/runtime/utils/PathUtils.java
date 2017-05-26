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
import java.net.MalformedURLException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;

/**
 * created by ycbai on 2017年5月23日 Detailled comment
 *
 */
public class PathUtils {

    public static final String FOLDER_COMPS = "components"; //$NON-NLS-1$

    public static final String FOLDER_INSTALLED = "installed"; //$NON-NLS-1$

    public static final String FOLDER_SHARED = "shared"; //$NON-NLS-1$

    private static final String P2_REP_FILE_URI_PATTERN = "^jar:(.+)!\\/$"; //$NON-NLS-1$

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

    public static URI getP2RepURIFromCompFile(File compFile) {
        if (compFile == null) {
            return null;
        }
        return URI.create("jar:" + compFile.toURI().toString() + "!/"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static File getCompFileFromP2RepURI(URI p2RepURI) throws MalformedURLException {
        if (p2RepURI == null) {
            return null;
        }
        String filePath = p2RepURI.toString();
        Matcher matcher = Pattern.compile(P2_REP_FILE_URI_PATTERN).matcher(filePath);
        if (matcher.find()) {
            filePath = matcher.group(1);
        }
        return new File(URI.create(filePath).toURL().getFile());
    }

}
