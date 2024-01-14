package org.example.performance.pojo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description
 * @date 2024/1/12 11:55:32
 */

@Data
public class FileInfo {
    MultipartFile file;
    int currentChunk;
    int totalChunks;
    String md5;
    String originalFilename;
}
