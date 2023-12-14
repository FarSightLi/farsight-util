package org.example.performance.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 磁盘信息BO
 * @date 2023/12/6 14:35:12
 */
@Data
public class DiskInfoVO {
    /**
     * 每个目录的详情
     */
    private List<DiskDetail> partitions;
    /**
     * 总容量（两位小数，自带单位，会计算为GB，MB等）
     */
    private String totalSize;

    /**
     * 已使用占比（%）
     */
    private BigDecimal usedRate;

    /**
     * 磁盘使用量（两位小数，自带单位，会计算为GB，MB等）
     */
    private String usedSize;

    @Data
    public static class DiskDetail {
        /**
         * 磁盘目录名
         */
        private String dfName;
        /**
         * 磁盘容量（MB）
         */
        private BigDecimal dfSize;

        /**
         * 磁盘使用占比（%）
         */
        private BigDecimal diskUsedRate;

        /**
         * 磁盘使用量（MB）
         */
        private BigDecimal diskUsedSize;

        /**
         * Inode使用占比（%）
         */
        private BigDecimal inodeUsedRate;

        /**
         * IO速率（%）
         */
        private BigDecimal ioRate;
    }
}