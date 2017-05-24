package org.talend.core.model.general;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.talend.core.IService;
import org.talend.core.nexus.NexusServerBean;

public interface INexusService extends IService {
	Map upload(URL source);

	Map upload(String version,URL source);

	Map upload(String repositoryURL, String username, String password, String groupId, String artifactId,
			String version,  URL source);

	Map getMavenMetadata(String groupId, String artifactId, String version);

	InputStream getContentInputStream(String r, String g, String a, String v);

    public NexusServerBean getTalendNexusServerBean(String repositoryId);

}
