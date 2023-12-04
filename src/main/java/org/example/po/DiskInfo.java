package org.example.po;

import lombok.Data;

import java.util.List;

@Data
public class DiskInfo {
    private List<DiskDetail> partitions;
    private String totalSize;
    private Double usedRate;
    private String usedSize;

    @Data
    public static class DiskDetail {
        private String dfName;
        private Double dfSize;
        private Double diskUsedRate;
        private Double diskUsedSize;
        private Double inodeUsedRate;
        private Double ioRate;
    }
}
