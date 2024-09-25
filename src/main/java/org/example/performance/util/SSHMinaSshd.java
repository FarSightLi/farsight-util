package org.example.performance.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.ClientBuilder;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceParser;
import org.apache.sshd.common.future.CancelOption;
import org.apache.sshd.common.session.SessionHeartbeatController;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.scp.client.CloseableScpClient;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.client.fs.SftpFileSystem;
import org.apache.sshd.sftp.client.fs.SftpPath;
import org.example.performance.pojo.DO.HostConfigDO;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.KeyPair;
import java.time.Duration;
import java.util.*;

/**
 * SSHMinaSshd
 *
 * @author hlq
 * @description 使用mina-sshd框架作为ssh客户端，非线程安全
 * @date 2023/7/12
 */
@Slf4j
public class SSHMinaSshd implements SSH {

    static SshClient sSshClient;

    public static int SSH_CONNECT_TIMEOUT;
    public static int SSH_READ_TIMEOUT;

    static {
        setSshTimeout(5000, 10000);
        System.setProperty("org.apache.sshd.config.nio2-read-timeout", "60000");
        start();
    }

    private static synchronized void start() {
        try {
            sSshClient = ClientBuilder.builder()
                    .serverKeyVerifier((clientSession, socketAddress, publicKey) -> true)
                    .build();
            sSshClient.setSessionHeartbeat(SessionHeartbeatController.HeartbeatType.IGNORE, Duration.ofSeconds(10));
            sSshClient.start();
            CoreModuleProperties.IDLE_TIMEOUT.set(sSshClient, Duration.ofMinutes(120));
        } catch (Throwable e) {
            log.error("ssh client start err!", e);
            throw new RuntimeException(e);
        }
    }

    public static void setSshTimeout(int connectTimeout, int readTimeout) {
        if (connectTimeout > 0) {
            SSHMinaSshd.SSH_CONNECT_TIMEOUT = connectTimeout;
            System.setProperty("org.apache.sshd.config.io-connect-timeout", String.valueOf(connectTimeout));
        }
        if (readTimeout > 0) {
            SSHMinaSshd.SSH_READ_TIMEOUT = readTimeout;
        }
    }

    public static void stop() {
        sSshClient.stop();
    }

    private static final SessionCache<String, SessionWrapper> SESSION_CACHE = new SessionCache<>(new HashMap<>());

    private HostConfigDO mHostConfigDO;
    private SessionWrapper mSessionWrapper;
    private boolean mConnectSuccess = false;
    private boolean mIsOk = false;
    /**
     * 是否使用缓存的session
     */
    private final boolean mUseCache;

    public SSHMinaSshd(HostConfigDO hostConfigDO) {
        this(hostConfigDO, true);
    }

    public SSHMinaSshd(HostConfigDO hostConfigDO, boolean useCache) {
        mHostConfigDO = hostConfigDO;
        mUseCache = useCache;
    }

    /**
     * 重启ssh客户端
     */
    public static void restart() {
        sSshClient.stop();
        start();
    }


    private SessionWrapper getSessionFromCache() {
        String key = StrUtil.format("{}@{}:{}", mHostConfigDO.getUserName(), mHostConfigDO.getIp(), mHostConfigDO.getPort());

        return SESSION_CACHE.get(key, sessionWrapper -> sessionWrapper.canReused(SSHMinaSshd.this), () -> {
            try {
                ClientSession session = createSession();
                SessionWrapper sessionWrapper = new SessionWrapper(key, session);
                sessionWrapper.addSSH(this);
                return sessionWrapper;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }

    private SessionWrapper createSessionWrapper() {
        try {
            return new SessionWrapper(null, createSession());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ClientSession getSession() {
        if (mSessionWrapper == null || !mSessionWrapper.isConnected()) {
            if (mUseCache) {
                mSessionWrapper = getSessionFromCache();
                mSessionWrapper.addSSH(this);
            } else {
                mSessionWrapper = createSessionWrapper();
            }
        }
        ClientSession clientSession = mSessionWrapper == null ? null : mSessionWrapper.mSession;
        if (clientSession != null) {
            if (!mConnectSuccess) {
                mConnectSuccess = true;
            }
            if (!mIsOk) {
                mIsOk = true;
            }
        } else {
            if (mConnectSuccess) {
                mConnectSuccess = false;
            }
            if (mIsOk) {
                mIsOk = false;
            }
        }
        return clientSession;
    }

    private ClientSession createSession() throws Exception {
        mConnectSuccess = false;
        ClientSession session = null;
        ConnectFuture connect = null;
        try {
            connect = sSshClient.connect(mHostConfigDO.getUserName(), mHostConfigDO.getIp(), mHostConfigDO.getPort());
            session = connect
                    .verify(SSH_CONNECT_TIMEOUT, CancelOption.CANCEL_ON_TIMEOUT, CancelOption.CANCEL_ON_INTERRUPT)
                    .getSession();
            if (connect.isConnected()) {
                mConnectSuccess = true;
            }
            String secret = mHostConfigDO.getSecret();
            Integer secretType = mHostConfigDO.getSecretType();
            if (secretType != null && secretType != 0) {
                Collection<KeyPair> keyPairs;
                KeyPairResourceParser keyPairResourceParser = SecurityUtils.getKeyPairResourceParser();
                if (secretType == 1) {
                    keyPairs = keyPairResourceParser.loadKeyPairs(null, Paths.get(secret), null);
                } else {
                    ByteArrayInputStream keyInput = new ByteArrayInputStream(secret.getBytes(StandardCharsets.UTF_8));
                    keyPairs = keyPairResourceParser.loadKeyPairs(null, NamedResource.ofName("id_rsa"), null, keyInput);
                }
                if (keyPairs != null && keyPairs.size() > 0) {
                    for (KeyPair keyPair : keyPairs) {
                        session.addPublicKeyIdentity(keyPair);
                    }
                } else {
                    log.error("createSession : load host's privateKey fail! host : {}", getHost());
                }
            } else {
                session.addPasswordIdentity(secret);
            }
            session.auth()
                    .verify(SSH_CONNECT_TIMEOUT, CancelOption.CANCEL_ON_TIMEOUT, CancelOption.CANCEL_ON_INTERRUPT);
            return session;
        } catch (Throwable e) {
            if (session != null) {
                session.close(true);
            }
            if (connect != null) {
                connect.cancel();
            }
            throw e;
        }
    }

    /**
     * 上传文件到远程主机
     *
     * @param localPath  本地文件路径
     * @param remotePath 远程文件路径
     */
    public boolean scpDownloadFile(String remotePath, String localPath) {
        if (!StringUtils.isEmpty(localPath) && !StringUtils.isEmpty(remotePath)) {
            ClientSession session = getSession();
            if (session != null) {
                try (CloseableScpClient scpClient = createScpClient(session)) {
                    scpClient.download(remotePath, localPath, ScpClient.Option.PreserveAttributes);
                    return true;
                } catch (Exception e) {
                    log.error("scpDownloadFile err! host : " + getHost() + ", localPath = " + localPath + ", e : " + e);
                }
            } else {
                log.warn("scpDownloadFile fail! create session fail! localPath = " + localPath + ", remotePath = " + remotePath + "host : " + getHost());
            }
        } else {
            log.warn("scpDownloadFile fail! invalid path ! localPath = " + localPath + ", remotePath = " + remotePath + "host : " + getHost());
        }
        return false;
    }

    /**
     * 上传文件到远程主机
     *
     * @param localPaths  本地文件路径要与远程文件路径一一对应
     * @param remotePaths
     */
    public boolean scpUploadFile(String[] localPaths, String[] remotePaths) {
        if (localPaths != null && remotePaths != null && localPaths.length > 0 && localPaths.length <= remotePaths.length) {
            ClientSession session = getSession();
            if (session != null) {
                try (CloseableScpClient scpClient = createScpClient(session)) {
                    for (int i = 0; i < localPaths.length; i++) {
                        String remotePath = remotePaths[i];
                        // scp只支持远程目标为目录的情况，因此如果是目录，就提取父目录
                        if (!Files.isDirectory(Paths.get(remotePath))) {
                            remotePath = Paths.get(remotePath).getParent().toString();
                        }
                        this.execCommand("mkdir -p " + remotePath);
                        log.info("在：{} 上创建了目录：{}", mHostConfigDO.getIp(), remotePath);
                        scpClient.upload(localPaths[i], remotePath, ScpClient.Option.PreserveAttributes);
                    }
                    return true;
                } catch (Exception e) {
                    log.error("scpUploadFile err! host : " + getHost() + ", localPath = " + Arrays.toString(localPaths) + ", e : " + e);
                }
            }
        } else {
            log.warn("scpUploadFile fail! invalid path ! localPaths = " + Arrays.toString(localPaths) + ", remotePaths = " + Arrays.toString(remotePaths) + "host : " + getHost());
        }
        return false;
    }

    private CloseableScpClient createScpClient(ClientSession session) {
        ScpClientCreator creator = ScpClientCreator.instance();
        ScpClient client = creator.createScpClient(session);
        return CloseableScpClient.singleSessionInstance(client);
    }

    @Override
    public String execCommand(String command) {
        return execCommand(command, SSH_READ_TIMEOUT);
    }


    @Override
    public String execCommand(String command, int timeout) {
        ClientSession session = getSession();
        if (session != null) {
            try (ChannelExec execChannel = session.createExecChannel(command)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream errOut = new ByteArrayOutputStream();
                execChannel.setOut(out);
                execChannel.setErr(errOut);
                execChannel.open().verify(SSH_CONNECT_TIMEOUT, CancelOption.CANCEL_ON_TIMEOUT, CancelOption.CANCEL_ON_INTERRUPT);
                execChannel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), timeout);
                if (errOut.size() > 0) {
                    //WARNING: Your password has expired.Password change required but no TTY available.
                    log.error("execCommand : cmd : {}, errOut : {}, host : {}", command, errOut.toString("utf-8"), getHost());
                }
                return out.toString("utf-8");
            } catch (IOException e) {
                log.error("execCommand err! host : " + getHost() + ", cmd = " + command + ", e : " + e);
            }
        }
        return null;
    }

    @Override
    public Integer execCommand(String command, OutputStream stdOut, OutputStream stdErr, int readTimeout) {
        ClientSession session = getSession();
        if (session != null) {
            try (ChannelExec execChannel = session.createExecChannel(command)) {
                execChannel.setOut(stdOut);
                execChannel.setErr(stdErr);
                execChannel.open().verify(SSH_CONNECT_TIMEOUT, CancelOption.CANCEL_ON_TIMEOUT, CancelOption.CANCEL_ON_INTERRUPT);
                Set<ClientChannelEvent> clientChannelEvents = execChannel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), readTimeout);
                if (clientChannelEvents.contains(ClientChannelEvent.TIMEOUT)) {
                    return SSH.TIMEOUT;
                }
                return execChannel.getExitStatus();
            } catch (IOException e) {
                log.error("execCommand err! host : " + getHost() + ", cmd = " + command + ", e : " + e);
            }
        } else {
            log.warn("execCommand err! get session fail， host : " + getHost() + ", cmd = " + command);
        }
        return null;
    }

    /**
     * 执行命令并检查结果
     *
     * @param cmd
     */
    public boolean exeCmdAndCheckResult(String cmd, int timeout) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Integer code = execCommand(cmd, outputStream, outputStream, timeout);
        if (code == null || code != 0) {
            try {
                log.error("命令执行结果检查失败: host: {}, code = {}, cmd:{}, out = {}", getHost(), code, cmd, outputStream.toString("utf-8"));
            } catch (Exception e) {
            }
            return false;
        }
        return true;
    }

    @Override
    public ChannelExec execCommand(String command, OutputStream outputStream, OutputStream errOut, boolean usePty) throws Exception {
        ClientSession session = getSession();
        if (session == null) {
            throw new RuntimeException("create.ssh.session.fail");
        }
        ChannelExec execChannel = null;
        try {
            execChannel = session.createExecChannel(command);
            execChannel.setOut(outputStream);
            execChannel.setErr(errOut);
            execChannel.setUsePty(usePty);
            execChannel.open().verify(SSH_CONNECT_TIMEOUT, CancelOption.CANCEL_ON_TIMEOUT, CancelOption.CANCEL_ON_INTERRUPT);
            return execChannel;
        } catch (IOException e) {
            if (execChannel != null) {
                execChannel.close();
            }
            log.error("execCommand err! host : " + getHost() + ", cmd = " + command + ", e : " + e);
            throw e;
        }
    }

    /**
     * 通过sftp给客户端下载远程文件
     *
     * @param remotePathStr
     * @param response
     */
    public void transferRemoteFilePath(String remotePathStr, HttpServletResponse response) {
        ClientSession session = getSession();
        if (session != null) {
            try (SftpFileSystem fs = createSftpFileSystem(session)) {
                SftpPath remotePath = fs.getPath(remotePathStr);
                if (!Files.exists(remotePath)) {
                    throw new RuntimeException("remote.file.not.exist");
                }
                long size = Files.size(remotePath);
                log.info("getAttributes: size = {}", size);
                response.setContentType("application/octet-stream");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + remotePath.getFileName() + "\"");
                response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
                response.setHeader(HttpHeaders.PRAGMA, "no-cache");
                response.setHeader(HttpHeaders.EXPIRES, "0");
                response.setContentLengthLong(size);
                Files.copy(remotePath, response.getOutputStream());
            } catch (Exception e) {
                log.error("sftpDownload err! host : " + getHost(), e);
                throw new RuntimeException("fail.transfer.fail");
            }
        }
        log.error("sftpDownload err! host : " + getHost() + ", get session fail!");
        throw new RuntimeException("host.connect.fail");
    }

    public boolean downloadFile(String srcRemotePath, String destLocalPath) {
        ClientSession session = getSession();
        if (session != null) {
            try (SftpFileSystem fs = createSftpFileSystem(session)) {
                SftpPath remotePath = fs.getPath(srcRemotePath);
                Path localPath = Paths.get(destLocalPath);
                if (Files.isDirectory(localPath)) {
                    SftpPath fileName = remotePath.getFileName();
                    if (fileName != null) {
                        localPath = Paths.get(destLocalPath, fileName.toString());
                    } else {
                        log.error("sftpDownload err! host : " + getHost() + ", couldn't get remotePath's fileName!");
                        return false;
                    }
                }
                Files.copy(remotePath, localPath, StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (Exception e) {
                log.error("sftpDownload err! host : " + getHost(), e);
                return scpDownloadFile(srcRemotePath, destLocalPath);
            }
        }
        log.error("sftpDownload err! host : " + getHost() + ", get session fail!");
        return false;
    }

    /**
     * 下载单个文件，并且计算进度
     * <p>
     * 下载字节数可以通过ProgressHelper.DOWN_BYTE_MAP获得
     *
     * @param sessionId     会话id，用于标记下载进度
     * @param totalSize     总大小（B）
     * @param srcRemotePath
     * @param localName     本地保存地址完整路径（即包含文件名）
     * @return
     */
    public boolean downloadAndGetProgress(Integer sessionId, Long totalSize,
                                          String srcRemotePath, String localName) {
        ClientSession session = getSession();
        if (session != null) {
            try (SftpFileSystem fs = createSftpFileSystem(session)) {
                SftpPath remotePath = fs.getPath(srcRemotePath);

                Path localPath = Paths.get(localName);

                try (InputStream inputStream = Files.newInputStream(remotePath);
                     OutputStream outputStream = Files.newOutputStream(localPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        if (sessionId != null && totalSize != null) {
                            // FS TODO
//                            ProgressHelper.DOWN_BYTE_MAP.merge(sessionId, (double) bytesRead, Double::sum);
                        }
                    }

                }

                return true;
            } catch (Exception e) {
                log.error("sftpDownload err! host :{}", getHost(), e);
            }
        } else {
            log.error("sftpDownload err! host : " + getHost() + ", get session fail!");
        }
        return false;
    }

    public boolean uploadFile(String[] srcLocalPath, String[] destRemotePath) {
        return uploadFile(srcLocalPath, destRemotePath, true);
    }

    public boolean uploadFile(String[] srcLocalPath, String[] destRemotePath, boolean useScpOnErr) {
        if (srcLocalPath == null || destRemotePath == null || srcLocalPath.length == 0 || srcLocalPath.length != destRemotePath.length) {
            log.error("sftpUpload err! host : " + getHost() + ", srcLocalPath or destRemotePath invalid!");
            return false;
        }
        ClientSession session = getSession();
        if (session != null) {
            try (SftpFileSystem fs = createSftpFileSystem(session)) {
                for (int i = 0; i < srcLocalPath.length; i++) {
                    SftpPath remotePath = fs.getPath(destRemotePath[i]);
                    Path localPath = Paths.get(srcLocalPath[i]);
                    // 如果远程目录不存在，就创建
                    SftpPath parent = remotePath.getParent();
                    if (!Files.exists(parent)) {
                        Files.createDirectories(parent);
                        log.info("在：{} 上创建了目录：{}", mHostConfigDO.getIp(), remotePath);
                    }
                    if (Files.isDirectory(remotePath)) {
                        Path fileName = localPath.getFileName();
                        if (fileName != null) {
                            remotePath = remotePath.resolve(fileName.toString());
                        } else {
                            log.error("sftpUpload err! host : " + getHost() + ", couldn't get localPath's fileName!");
                            return false;
                        }
                    }
                    Files.copy(localPath, remotePath, StandardCopyOption.REPLACE_EXISTING);
                }
                return true;
            } catch (Exception e) {
                log.error("sftpUpload err ：{} host {} ", e, getHost());
                if (useScpOnErr) {
                    return scpUploadFile(srcLocalPath, destRemotePath);
                }
            }
        } else {
            log.error("sftpUpload err! host : " + getHost() + ", get session fail!");
        }
        return false;
    }

    private SftpFileSystem createSftpFileSystem(ClientSession session) throws IOException {
        return SftpClientFactory.instance().createSftpFileSystem(session, 8192, 8192);
    }

    @Override
    public String getHost() {
        return mHostConfigDO.getIp();
    }

    public String getUsername() {
        return mHostConfigDO.getUserName();
    }

    @Override
    public void tryGetSession() throws SshException {
        try {
            getSession();
        } catch (Exception e) {
            Throwable t = e;
            while ((t = t.getCause() == t ? null : t.getCause()) != null) {
            }
            getSession();
        }
    }

    /**
     * 探活
     *
     * @return
     */
    @Override
    public boolean isOpen() {
        return mConnectSuccess;
//                || SocketRetryUtil.retryConnect(mHostConfigDO.getIp(), mHostConfigDO.getPort(), 500);
    }

    /**
     * ssh是否已正常登录
     *
     * @return
     */
    public boolean isOk() {
        return mIsOk;
    }

    @Override
    public boolean isRoot() {
        return "root".equals(mHostConfigDO.getUserName());
    }

    @Override
    public void close() throws IOException {
        mConnectSuccess = false;
        mIsOk = false;
        if (mSessionWrapper != null) {
            mSessionWrapper.closeFromSSH(this);
            mSessionWrapper = null;
        }
    }

    private static class SessionWrapper {
        private String mKey;
        private ClientSession mSession;
        private HashSet<SSH> mSSHList;

        public SessionWrapper(String key, ClientSession session) {
            mKey = key;
            mSession = session;
        }

        public synchronized boolean canReused(SSH ssh) {
            if (isConnected()) {
                addSSH(ssh);
                return true;
            }
            return false;
        }

        public boolean isConnected() {
            ClientSession session = mSession;
            boolean isOpen;
            if (session != null) {
                isOpen = session.isOpen();
                if (!isOpen) {
                    closeAll();
                }
            } else {
                isOpen = false;
            }
            return isOpen;
        }

        public synchronized void addSSH(SSH ssh) {
            if (mSSHList == null) {
                mSSHList = new HashSet<>();
            }
            mSSHList.add(ssh);
        }

        public synchronized void closeFromSSH(SSH ssh) {
            if (mSSHList != null) {
                mSSHList.remove(ssh);
            }
            if (mKey == null) {
                closeSession();
            } else if (mSSHList == null || mSSHList.isEmpty()) {
                closeSession();
                SESSION_CACHE.remove(mKey);
                mSSHList = null;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("close : can't close session : {}, sshes.size : {} , host : {}", mSession, mSSHList.size(), ssh.getHost());
                }
            }
        }

        private void closeSession() {
            if (mSession != null) {
                try {
                    mSession.close(true);
                } catch (Exception e) {
                    log.error("closeSession err! host : {}, e : " + e, mKey);
                }
                if (log.isDebugEnabled()) {
                    log.debug("close : close session success : {}, host : {}", mSession, mSession.getRemoteAddress());
                }
                mSession = null;
            }
        }

        public synchronized void closeAll() {
            closeSession();
            if (mSSHList != null) {
                mSSHList.clear();
                mSSHList = null;
            }
            if (mKey != null) {
                SESSION_CACHE.remove(mKey);
            }
        }
    }
}
