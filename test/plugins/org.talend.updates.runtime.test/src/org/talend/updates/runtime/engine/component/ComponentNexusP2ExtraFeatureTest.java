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

import org.junit.Assert;
import org.junit.Test;
import org.talend.updates.runtime.model.P2ExtraFeatureException;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ComponentNexusP2ExtraFeatureTest {

    static class ComponentNexusP2ExtraFeatureTestClass extends ComponentNexusP2ExtraFeature {

        public ComponentNexusP2ExtraFeatureTestClass() {
            super();
        }

        public ComponentNexusP2ExtraFeatureTestClass(String name, String version) {
            super(name, version, null, null, null, null);
        }

        public ComponentNexusP2ExtraFeatureTestClass(String name, String version, String description, String product,
                String mvnURI, String p2IuId) {
            super(name, version, description, product, mvnURI, p2IuId);
        }

        @Override
        public String getP2ProfileId() {
            return "profile"; //$NON-NLS-1$
        }

    }

    @Test
    public void test_getNexusURL() {
        ComponentNexusP2ExtraFeatureTestClass feature = new ComponentNexusP2ExtraFeatureTestClass();
        Assert.assertNull(feature.getNexusURL());

        final String KEY = ComponentNexusP2ExtraFeatureTestClass.PROP_KEY_NEXUS_URL;
        String oldValue = System.getProperty(KEY);
        try {
            System.setProperty(KEY, "http://abc.com");
            Assert.assertNotNull(feature.getNexusURL());
            Assert.assertEquals("http://abc.com", feature.getNexusURL());
        } finally {
            if (oldValue == null) {
                System.getProperties().remove(KEY);
            } else {
                System.setProperty(KEY, oldValue);
            }
        }
    }

    @Test
    public void test_getNexusUser() {
        ComponentNexusP2ExtraFeatureTestClass feature = new ComponentNexusP2ExtraFeatureTestClass();
        Assert.assertNull(feature.getNexusUser());

        final String KEY = ComponentNexusP2ExtraFeatureTestClass.PROP_KEY_NEXUS_USER;
        String oldValue = System.getProperty(KEY);
        try {
            System.setProperty(KEY, "admin");
            Assert.assertNotNull(feature.getNexusUser());
            Assert.assertEquals("admin", feature.getNexusUser());
        } finally {
            if (oldValue == null) {
                System.getProperties().remove(KEY);
            } else {
                System.setProperty(KEY, oldValue);
            }
        }
    }

    @Test
    public void test_getNexusPass() {
        ComponentNexusP2ExtraFeatureTestClass feature = new ComponentNexusP2ExtraFeatureTestClass();
        Assert.assertNull(feature.getNexusPass());

        final String KEY = ComponentNexusP2ExtraFeatureTestClass.PROP_KEY_NEXUS_PASS;
        String oldValue = System.getProperty(KEY);
        try {
            System.setProperty(KEY, "talend");
            Assert.assertNotNull(feature.getNexusPass());
            Assert.assertEquals("talend", new String(feature.getNexusPass()));
        } finally {
            if (oldValue == null) {
                System.getProperties().remove(KEY);
            } else {
                System.setProperty(KEY, oldValue);
            }
        }
    }

    @Test
    public void test_isInstalled_emptyInstallVersion() throws P2ExtraFeatureException {
        // null
        ComponentNexusP2ExtraFeatureTestClass feature = new ComponentNexusP2ExtraFeatureTestClass("tJava", null);
        Assert.assertFalse(feature.isInstalled(null));

        // emtpy version
        feature = new ComponentNexusP2ExtraFeatureTestClass("tJava", "");
        Assert.assertFalse(feature.isInstalled(null));
    }

}
