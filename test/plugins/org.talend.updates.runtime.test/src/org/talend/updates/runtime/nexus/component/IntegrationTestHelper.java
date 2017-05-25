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
package org.talend.updates.runtime.nexus.component;

import org.talend.core.nexus.NexusServerBean;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class IntegrationTestHelper {

    /**
     * this test is release nexus URI.
     */
    public static NexusServerBean getNexusServerReleaseBean() {

        String nexusURL = "http://192.168.33.29:8081/nexus/content/repositories/components/";
        String nexusUser = "admin";
        String nexusPass = "Talend123";

        final NexusServerBean nexusServerBean = new NexusServerBean();
        nexusServerBean.setRepositoryBaseURI(nexusURL);
        nexusServerBean.setUserName(nexusUser);
        nexusServerBean.setPassword(nexusPass);

        return nexusServerBean;
    }
}
