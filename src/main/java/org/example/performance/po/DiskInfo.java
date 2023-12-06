package org.example.performance.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DiskInfo {
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
