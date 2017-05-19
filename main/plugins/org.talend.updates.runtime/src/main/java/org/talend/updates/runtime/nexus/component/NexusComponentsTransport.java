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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.network.NetworkUtil;
import org.talend.core.nexus.NexusServerUtils;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.updates.runtime.engine.HttpClientTransport;

public class NexusComponentsTransport {

    private static final int BUFFER_SIZE = 8192;

    private String nexusURL, nexusUser;

    private char[] nexusPass;

    public NexusComponentsTransport(String nexusURL, String nexusUser, char[] nexusPass) {
        this.nexusURL = nexusURL;
        this.nexusUser = nexusUser;
        this.nexusPass = nexusPass;
    }

    protected String getNexusURL() {
        return nexusURL;
    }

    protected String getNexusUser() {
        return nexusUser;
    }

    protected String getNexusPassStr() {
        if (nexusPass == null) {
            return null;
        }
        return new String(nexusPass);

    }

    public NexusComponentsTransport(String nexusUrl) {
        this(nexusUrl, null, null);
    }

    public boolean isAvailable() {
        if (getNexusURL() == null || getNexusURL().isEmpty()) {
            return false;
        }
        return NexusServerUtils.checkConnectionStatus(getNexusURL(), getNexusUser(), getNexusPassStr());
    }

    public void downloadFile(IProgressMonitor monitor, String mvnURI, File target) throws Exception {
        MavenArtifact artifact = MavenUrlHelper.parseMvnUrl(mvnURI);
        downloadFile(monitor, artifact, target);
    }

    private void setAuthenticator() {
        if (StringUtils.isNotEmpty(getNexusUser())) {
            Authenticator.setDefault(new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    final String nexusPassStr = getNexusPassStr();
                    return new PasswordAuthentication(getNexusUser(), nexusPassStr != null ? nexusPassStr.toCharArray()
                            : new char[0]);
                }

            });
        }
    }

    public void downloadFile(IProgressMonitor monitor, MavenArtifact artifact, File target) throws Exception {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        if (artifact == null) {
            return;
        }
        final String reletivePath = PomUtil.getArtifactPath(artifact);
        if (reletivePath == null) {
            return;
        }

        final Authenticator defaultAuthenticator = NetworkUtil.getDefaultAuthenticator();

        boolean success = false;

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        HttpURLConnection httpURLConnection = null;
        try {
            setAuthenticator();

            httpURLConnection = NexusServerUtils.getHttpURLConnection(getNexusURL(), reletivePath, getNexusUser(),
                    getNexusPassStr());

            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            InputStream inputStream = httpURLConnection.getInputStream();
            bis = new BufferedInputStream(inputStream);
            bos = new BufferedOutputStream(new FileOutputStream(target));

            byte[] buf = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = bis.read(buf)) != -1) {
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                bos.write(buf, 0, bytesRead);
            }
            bos.flush();
            success = true;
        } finally {
            //
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }

            //
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }

            //
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            // delete the failed file
            if (!success && target.exists() && target.isFile()) {
                target.delete();
            }

            // set back
            Authenticator.setDefault(defaultAuthenticator);
        }
    }

    public Document downloadXMLDocument(IProgressMonitor monitor, MavenArtifact artifact) throws Exception {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        if (artifact == null) {
            return null;
        }
        final String reletivePath = PomUtil.getArtifactPath(artifact);
        if (reletivePath == null) {
            return null;
        }

        final Authenticator defaultAuthenticator = NetworkUtil.getDefaultAuthenticator();

        BufferedInputStream bis = null;
        HttpURLConnection httpURLConnection = null;
        try {
            setAuthenticator();

            httpURLConnection = NexusServerUtils.getHttpURLConnection(getNexusURL(), reletivePath, getNexusUser(),
                    getNexusPassStr());

            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            InputStream inputStream = httpURLConnection.getInputStream();
            bis = new BufferedInputStream(inputStream);
            SAXReader saxReader = new SAXReader();

            Document document = saxReader.read(bis);
            return document;
        } finally {
            //
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }

            //
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            // set back
            Authenticator.setDefault(defaultAuthenticator);
        }

    }

    /**
     * 
     * DOC ggu Comment method "doHttpUpload".
     * 
     * upload the file to remote nexus, and base one the maven artifact.
     * 
     * @param monitor
     * @param uploadFile
     * @param artifact
     * @throws Exception
     */
    public void doHttpUpload(IProgressMonitor monitor, final File uploadFile, final MavenArtifact artifact) throws Exception {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        if (uploadFile == null) {
            throw new IllegalArgumentException("Must provide the upload file, can't be null"); //$NON-NLS-1$
        }
        if (!uploadFile.exists() || !uploadFile.isFile()) {
            throw new IllegalArgumentException("The upload file is invalid: " + uploadFile); //$NON-NLS-1$
        }
        if (artifact == null) {
            throw new IllegalArgumentException("Must provide the maven artifact, can't be null"); //$NON-NLS-1$
        }

        new HttpClientTransport(getNexusURL(), getNexusUser(), getNexusPassStr()) {

            @Override
            protected HttpResponse execute(IProgressMonitor monitor, DefaultHttpClient httpClient, URI targetURI)
                    throws Exception {
                HttpPut httpPut = new HttpPut(targetURI.toString());
                httpPut.setEntity(new FileEntity(uploadFile));
                HttpResponse response = httpClient.execute(httpPut);
                return response;
            }
        }.doRequest(monitor, artifact);
    }

    /**
     * 
     * DOC ggu Comment method "doHttpDownload".
     * 
     * download the maven artifact from the remote nexus to target file, if download falure, will remove the unfinished
     * file.
     * 
     * @param monitor
     * @param targetFile
     * @param artifact
     * @throws Exception
     */
    public void doHttpDownload(IProgressMonitor monitor, File targetFile, MavenArtifact artifact) throws Exception {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        if (targetFile == null) {
            throw new IllegalArgumentException("Must provide the target file, can't be null"); //$NON-NLS-1$
        }
        if (artifact == null) {
            throw new IllegalArgumentException("Must provide the download artifact, can't be null"); //$NON-NLS-1$
        }

        new HttpClientTransport(getNexusURL(), getNexusUser(), getNexusPassStr()) {

            @Override
            protected HttpResponse execute(IProgressMonitor monitor, DefaultHttpClient httpClient, URI targetURI)
                    throws Exception {
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                HttpGet httpGet = new HttpGet(targetURI);
                HttpResponse response = httpClient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    // final String fileName = getFileName(response);

                    BufferedOutputStream fileStream = null;
                    InputStream httpStream = null;

                    boolean downloaded = false;
                    try {
                        HttpEntity entity = response.getEntity();
                        httpStream = entity.getContent();

                        fileStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                        byte[] buffer = new byte[1024 * 8];
                        int ch = 0;
                        while ((ch = httpStream.read(buffer)) != -1) {
                            fileStream.write(buffer, 0, ch);

                            if (monitor.isCanceled()) {
                                throw new OperationCanceledException();
                            }
                        }
                        fileStream.flush();

                        downloaded = true;
                    } finally {
                        if (fileStream != null) {
                            try {
                                fileStream.close();
                            } catch (IOException e) {
                                //
                            }
                        }
                        if (httpStream != null) {
                            try {
                                httpStream.close();
                            } catch (IOException e) {
                                //
                            }
                        }
                        if (!downloaded) { // download failure
                            if (targetFile.exists()) {
                                targetFile.delete(); // remove the wrong file.
                            }
                        }
                    }
                }
                return response;
            }
        }.doRequest(monitor, artifact);
    }

    private String getFileName(HttpResponse response) {
        Header contentHeader = response.getFirstHeader("Content-Disposition");//$NON-NLS-1$
        String filename = null;
        if (contentHeader != null) {
            HeaderElement[] values = contentHeader.getElements();
            if (values.length == 1) {
                NameValuePair param = values[0].getParameterByName("filename"); //$NON-NLS-1$
                if (param != null) {
                    // filename = new String(param.getValue().toString().getBytes(), "utf-8");
                    // filename=URLDecoder.decode(param.getValue(),"utf-8");
                    filename = param.getValue();
                }
            }
        }
        return filename;
    }

    /**
     * 
     * DOC ggu Comment method "isAvailable".
     * 
     * check the maven artifact existed on remote nexus or not.
     * 
     * @param monitor
     * @param artifact
     * @return
     */
    public boolean isAvailable(IProgressMonitor monitor, final MavenArtifact artifact) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        if (artifact == null) {
            return false;
        }

        try {
            new HttpClientTransport(getNexusURL(), getNexusUser(), getNexusPassStr()) {

                @Override
                protected HttpResponse execute(IProgressMonitor monitor, DefaultHttpClient httpClient, URI targetURI)
                        throws Exception {
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    HttpGet httpGet = new HttpGet(targetURI);
                    HttpResponse response = httpClient.execute(httpGet);
                    final int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) { // 200
                        // ok
                    } else if (statusCode == HttpStatus.SC_BAD_REQUEST) { // 400?
                        throw new BusinessException("The URI is not existed ");
                    }

                    return response;
                }
            }.doRequest(monitor, artifact);

            return true; // no any exception?
        } catch (Exception e) {
            if (CommonsPlugin.isDebugMode()) {
                ExceptionHandler.process(e);
            }
        }
        return false;
    }
}
