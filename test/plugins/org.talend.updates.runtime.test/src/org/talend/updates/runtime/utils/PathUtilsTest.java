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
package org.talend.updates.runtime.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.junit.Test;

/**
 * created by ycbai on 2017年5月23日 Detailled comment
 *
 */
public class PathUtilsTest {

    @Test
    public void testGetComponentsFolder() throws IOException {
        File componentsFolder = PathUtils.getComponentsFolder();
        assertNotNull(componentsFolder);
        assertTrue(componentsFolder.exists());
        File expectedFolder = new File(Platform.getConfigurationLocation().getDataArea(PathUtils.FOLDER_COMPS).getPath());
        assertEquals(expectedFolder, componentsFolder);
    }

    @Test
    public void testGetComponentsInstalledFolder() throws IOException {
        File intalledFolder = PathUtils.getComponentsInstalledFolder();
        assertNotNull(intalledFolder);
        assertTrue(intalledFolder.exists());
        File expectedFolder = new File(PathUtils.getComponentsFolder(), PathUtils.FOLDER_INSTALLED);
        assertEquals(expectedFolder, intalledFolder);
    }

    @Test
    public void testgGetComponentsSharedFolder() throws IOException {
        File sharedFolder = PathUtils.getComponentsSharedFolder();
        assertNotNull(sharedFolder);
        assertTrue(sharedFolder.exists());
        File expectedFolder = new File(PathUtils.getComponentsFolder(), PathUtils.FOLDER_SHARED);
        assertEquals(expectedFolder, sharedFolder);
    }

}
