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
package org.talend.updates.runtime.engine.component;

import java.io.File;
import java.io.IOException;

import org.eclipse.m2e.core.MavenPlugin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.commons.runtime.service.ComponentsInstallComponent;
import org.talend.commons.utils.resource.BundleFileUtil;
import org.talend.librariesmanager.prefs.LibrariesManagerUtils;
import org.talend.updates.runtime.engine.P2InstallerTest;
import org.talend.utils.io.FilesUtils;

/**
 * created by ycbai on 2017年5月18日 Detailled comment
 *
 */
public class ComponentP2ExtraFeatureTest {

    private ComponentP2ExtraFeature feature;

    private File tmpFolder = null;

    @Before
    public void before() throws IOException {
        feature = new ComponentP2ExtraFeature();
        tmpFolder = org.talend.utils.files.FileUtils.createTmpFolder("test", "comp"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @After
    public void after() {
        FilesUtils.deleteFolder(tmpFolder, true);
    }

    @Test
    public void test_syncLibraries_nonExisted() throws IOException {
        final String oldLibPath = System.getProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH);
        try {
            // set prod lib
            final File testProdLibFolder = new File(tmpFolder, "prod/" + LibrariesManagerUtils.LIB_JAVA_SUB_FOLDER);
            System.setProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH, testProdLibFolder.getAbsolutePath());
            Assert.assertFalse(testProdLibFolder.exists());

            final File testFile = new File(tmpFolder, "test");
            feature.syncLibraries(testFile);
            Assert.assertFalse(testProdLibFolder.exists());
        } finally {
            if (oldLibPath != null) {
                System.setProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH, oldLibPath);
            } else {
                System.clearProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH);
            }

        }
    }

    @Test
    public void test_syncLibraries_nonFolder() throws IOException {
        final String oldLibPath = System.getProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH);
        try {
            // set prod lib
            final File testProdLibFolder = new File(tmpFolder, "prod/" + LibrariesManagerUtils.LIB_JAVA_SUB_FOLDER);
            System.setProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH, testProdLibFolder.getAbsolutePath());
            Assert.assertFalse(testProdLibFolder.exists());

            final File testFile = new File(tmpFolder, "test.txt");
            testFile.createNewFile();

            feature.syncLibraries(testFile);
            Assert.assertFalse(testProdLibFolder.exists());
        } finally {
            if (oldLibPath != null) {
                System.setProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH, oldLibPath);
            } else {
                System.clearProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH);
            }

        }
    }

    @Test
    public void test_syncLibraries() throws IOException {
        final File testDataFile = BundleFileUtil.getBundleFile(this.getClass(), P2InstallerTest.TEST_COMP_MYJIRA);
        Assert.assertNotNull(testDataFile);
        Assert.assertTrue(testDataFile.exists());
        final String oldLibPath = System.getProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH);
        try {
            // set prod lib
            final File testProdLibFolder = new File(tmpFolder, "prod/" + LibrariesManagerUtils.LIB_JAVA_SUB_FOLDER);
            System.setProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH, testProdLibFolder.getAbsolutePath());

            final File testFolder = new File(tmpFolder, "test");
            final File testLibFolder = new File(testFolder, LibrariesManagerUtils.LIB_JAVA_SUB_FOLDER);
            FilesUtils.copyFile(testDataFile, new File(testLibFolder, testDataFile.getName()));

            Assert.assertFalse(testProdLibFolder.exists());

            feature.syncLibraries(testFolder);

            Assert.assertTrue(testProdLibFolder.exists());
            final String[] list = testProdLibFolder.list();
            Assert.assertEquals(1, list.length);
            Assert.assertEquals(testDataFile.getName(), list[0]);
        } finally {
            if (oldLibPath != null) {
                System.setProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH, oldLibPath);
            } else {
                System.clearProperty(LibrariesManagerUtils.TALEND_LIBRARY_PATH);
            }

        }
    }

    @Test
    public void test_syncM2Repository() throws Exception {
        final File testDataFile = BundleFileUtil.getBundleFile(this.getClass(), P2InstallerTest.TEST_COMP_MYJIRA);
        Assert.assertNotNull(testDataFile);
        Assert.assertTrue(testDataFile.exists());

        FilesUtils.unzip(testDataFile.getAbsolutePath(), tmpFolder.getAbsolutePath());

        // check from local
        final String localRepositoryPath = MavenPlugin.getMaven().getLocalRepositoryPath();
        Assert.assertNotNull(localRepositoryPath);
        File localRepoFolder = new File(localRepositoryPath);
        Assert.assertTrue(localRepoFolder.exists());

        File libFile = new File(localRepoFolder, "org/talend/libraries/mytest/6.0.0/mytest-6.0.0.jar");
        if (libFile.exists()) {
            libFile.delete();
        }
        File pomFile = new File(localRepoFolder, "org/talend/libraries/mytest/6.0.0/mytest-6.0.0.pom");
        if (pomFile.exists()) {
            pomFile.delete();
        }

        feature.syncM2Repository(tmpFolder);

        Assert.assertTrue("sync lib for M2 repository failure", libFile.exists());
        Assert.assertTrue(pomFile.exists());
    }

    @Test
    public void test_syncM2Repository_login() throws Exception {
        final File testDataFile = BundleFileUtil.getBundleFile(this.getClass(), P2InstallerTest.TEST_COMP_MYJIRA);
        Assert.assertNotNull(testDataFile);
        Assert.assertTrue(testDataFile.exists());

        FilesUtils.unzip(testDataFile.getAbsolutePath(), tmpFolder.getAbsolutePath());

        // check from local
        final String localRepositoryPath = MavenPlugin.getMaven().getLocalRepositoryPath();
        Assert.assertNotNull(localRepositoryPath);
        File localRepoFolder = new File(localRepositoryPath);
        Assert.assertTrue(localRepoFolder.exists());

        final String libPath = "org/talend/libraries/mytest/6.0.0/mytest-6.0.0.jar";
        File libFile = new File(localRepoFolder, libPath);
        if (libFile.exists()) {
            libFile.delete();
        }
        File pomFile = new File(localRepoFolder, "org/talend/libraries/mytest/6.0.0/mytest-6.0.0.pom");
        if (pomFile.exists()) {
            pomFile.delete();
        }

        feature.setLogin(true);
        feature.syncM2Repository(tmpFolder);

        Assert.assertFalse(libFile.exists());
        Assert.assertFalse(pomFile.exists());

        final File tempM2RepoFolder = feature.getTempM2RepoFolder();
        Assert.assertNotNull(tempM2RepoFolder);
        Assert.assertTrue(tempM2RepoFolder.exists());

        File tmpLibFile = new File(tempM2RepoFolder, ComponentsInstallComponent.FOLDER_M2_REPOSITORY + '/' + libPath);
        Assert.assertTrue(tmpLibFile.exists()); // in temp
        Assert.assertFalse(libFile.exists());// no install yet
    }

}
