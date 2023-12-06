package org.example.performance.pojo.bo;

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
public class DiskInfoBO {
    private List<DiskDetail> partitions;
    private BigDecimal totalSize;
    private BigDecimal usedRate;
    private BigDecimal usedSize;

    @Data
    public static class DiskDetail {
        private String dfName;
        private BigDecimal dfSize;
        private BigDecimal diskUsedRate;
        private BigDecimal diskUsedSize;
        private BigDecimal inodeUsedRate;
        private BigDecimal ioRate;
    }
}