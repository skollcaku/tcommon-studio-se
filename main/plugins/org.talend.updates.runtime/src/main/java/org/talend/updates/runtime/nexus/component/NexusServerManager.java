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
package org.talend.updates.runtime.nexus.component;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.INexusService;
import org.talend.core.nexus.NexusServerBean;

/**
 * created by ycbai on 2017年5月22日 Detailled comment
 *
 */
public class NexusServerManager {

    private static final String REPOSITORY_ID = "releases"; //$NON-NLS-1$

    private static NexusServerManager instance;

    private NexusServerManager() {
    }

    public static synchronized NexusServerManager getInstance() {
        if (instance == null) {
            instance = new NexusServerManager();
        }
        return instance;
    }

    public NexusServerBean getLocalNexusServer() {
        INexusService nexusService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(INexusService.class)) {
            nexusService = (INexusService) GlobalServiceRegister.getDefault().getService(INexusService.class);
        }
        if (nexusService == null) {
            return null;
        }
        return nexusService.getTalendNexusServerBean(REPOSITORY_ID);
    }

}
