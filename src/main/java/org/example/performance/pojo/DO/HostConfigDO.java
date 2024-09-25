package org.example.performance.pojo.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@TableName(value = "t_skyeye_host_config")
public class HostConfigDO implements Serializable {

    /**
     * 主键ID，自增长
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 主机IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 主机SSH端口
     */
    @TableField("port")
    private Integer port;

    /**
     * 探针端口
     */
    @TableField("agent_port")
    private Integer agentPort;

    /**
     * ssh账号
     */
    @TableField("user_name")
    private String userName;

    /**
     * ssh密码或秘钥文件路径
     */
    @TableField("secret")
    private String secret;

    /**
     * 状态 0:启用  1:删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 来源 0 host.ini 1 页面添加
     */
    @TableField("source")
    private Integer source;

    /**
     * 密码来源 0 密码(默认为0) 1 秘钥文件路径 2 秘钥文本
     */
    @TableField("secret_type")
    private Integer secretType = 0;

    /**
     * 实例唯一编号
     */
    @TableField(exist = false)
    private Long uniqueCode;

    /**
     * 主机在线状态
     */
    @TableField(exist = false)
    private Integer hostStatus;

    /**
     * CPU架构
     */
    @TableField(exist = false)
    private String cpuArch;

    /**
     * 容器名
     */
    @TableField(exist = false)
    private String containerName;

    /**
     * 主机名
     */
    @TableField(exist = false)
    private String hostName;

    public HostConfigDO() {
        super();
    }

    public HostConfigDO(String ip, Integer port, String userName, String secret) {
        this.ip = ip;
        this.port = port;
        this.userName = userName;
        this.secret = secret;
        this.status = 0;
        this.updateTime = this.createTime = new Date();
        adjustSecretType(secret);
    }

    public HostConfigDO(String ip, Integer port, Integer agentPort, String userName, String secret, Integer secretType) {
        this.ip = ip;
        this.port = port;
        this.agentPort = agentPort;
        this.userName = userName;
        this.secret = secret;
        this.secretType = secretType;
        this.status = 0;
        this.updateTime = this.createTime = new Date();
    }

    private void adjustSecretType(String secret) {
        if (StringUtils.hasLength(secret) && secret.startsWith("/")) {
            //秘钥文件路径
            this.secretType = 1;
        } else {
            //密码文本
            this.secretType = 0;
        }
    }

    //按IP和端口去重
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HostConfigDO)) return false;
        HostConfigDO that = (HostConfigDO) o;
        return ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip);
    }

    @Override
    public String toString() {
        return "{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", agentPort=" + agentPort +
                ", userName='" + userName + '\'' +
                ", secret='" + secret + '\'' +
                ", source=" + source +
                ", secretType=" + secretType +
                '}';
    }

    /**
     * 主机状态 0:启用 1:删除
     */
    public static String getStatusMapping(Integer id) {
        String value = "";
        if (id == 0) {
            value = "启用";
        } else if (id == 1) {
            value = "删除";
        }
        return value;
    }
}
