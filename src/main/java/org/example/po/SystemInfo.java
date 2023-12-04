package org.example.po;

import lombok.Data;

import java.util.List;


@Data
public class SystemInfo {
    private String cpuArch;
    private Integer cpuCores;
    private DiskInfo diskDetail;
    private String hostName;
    private String ip;
    private String kernelRelease;
    private String lastRestartTime;
    private String memSize;
    private Long runDuration;
    private Integer state;
    private String sysVersion;

    @Data
    public class DiskInfo {
        private List<DiskDetail> partitions;
        private String totalSize;
        private Double usedRate;
        private String usedSize;

        // Getter and Setter methods

        // Constructors

        @Data
        public class DiskDetail {
            private String dfName;
            private Double dfSize;
            private Double diskUsedRate;
            private Double diskUsedSize;
            private Double inodeUsedRate;
            private Double ioRate;
        }
    }


}
