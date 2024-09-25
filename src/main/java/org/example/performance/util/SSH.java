package org.example.performance.util;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.common.SshException;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * SSH
 *
 * @author hlq
 * @description sshxcute、Ganymed、jsch、mina-sshd
 * @date 2023/5/16
 */
public interface SSH extends Closeable {
    /**
     * 执行失败退出码
     */
    int FAILURE = -1;
    /**
     * 执行超时退出码
     */
    int TIMEOUT = -2;

    /**
     * 执行单个命令行
     * @param command
     * @return
     */
    String execCommand(String command);

    String execCommand(String command,int timeout);

    /**
     * 持续通道传输
     * @param command
     * @param outputStream
     * @param errOut
     * @param usePty
     * @return
     * @throws IOException
     */
    ChannelExec execCommand(String command, OutputStream outputStream,OutputStream errOut,boolean usePty) throws Exception;
    /**
     * 执行单个命令
     * @param command 命令内容
     * @param stdOut 结果输出
     * @param stdErr 错误输出
     * @param readTimeout 执行超时时间
     * @return 结果码，null和非0均为执行失败
     */
    Integer execCommand(String command, OutputStream stdOut, OutputStream stdErr, int readTimeout);

    /**
     * 执行命令并检查结果
     * @param cmd
     * @param timeout
     * @return
     */
    boolean exeCmdAndCheckResult(String cmd, int timeout);

    /**
     * 上传文件到远程主机
     * @param localPaths 本地文件路径要与远程文件路径一一对应
     * @param remotePaths
     */
    boolean scpUploadFile(String[] localPaths, String[] remotePaths);

    /**
     * 下载远程文件到本地路径
     * @param remotePath
     * @param localPath
     * @return
     */
    boolean scpDownloadFile(String remotePath, String localPath);

    /**
     * 通过sftp上传文件到远程主机，sftp失败后切换scp
     * @param srcLocalPath
     * @param destRemotePath
     * @return
     */
    boolean uploadFile(String[] srcLocalPath, String[] destRemotePath);

    /**
     * 通过sftp上传文件到远程主机，sftp失败后切换scp
     * @param srcLocalPath
     * @param destRemotePath
     * @return
     */
    boolean uploadFile(String[] srcLocalPath, String[] destRemotePath, boolean useScpOnErr);

    /**
     * 通过sftp下载远程文件到本地路径，sftp失败后切换scp
     * @param srcRemotePath
     * @param destLocalPath
     * @return
     */
    boolean downloadFile(String srcRemotePath, String destLocalPath);

    /**
     * 获取主机地址
     * @return
     */
    String getHost();

    /**
     * 获取用户名
     * @return
     */
    String getUsername();

    /**
     * 尝试连接
     */
    void tryGetSession() throws SshException;

    /**
     * socket探活
     * @return
     */
    boolean isOpen();

    /**
     * ssh是否已正常登录
     * @return
     */
    boolean isOk();

    /**
     * 判断是否为root用户
     * @return
     */
    boolean isRoot();
}
