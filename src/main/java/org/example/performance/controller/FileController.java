package org.example.performance.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.example.performance.component.Result;
import org.example.performance.component.aop.NotIdentify;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description
 * @date 2024/1/12 09:48:55
 */
@RestController
@CrossOrigin(origins = "*")
@Slf4j
@NotIdentify
public class FileController {
    private static final String UPLOAD_DIR = "D:\\temp";
    private static final String PATH_FLAG = "\\";
    private static final String SUFFIX = "_temp";

    @PostMapping("/upload")
    public Result<?> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("currentChunk") int currentChunk,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("md5") String md5,
            @RequestParam("originalFilename") String originalFilename) {

        try {
            // 检查MD5
            boolean md5Check = checkMD5(file, md5);

            if (!md5Check) {
                throw new BusinessException(CodeMsg.SYSTEM_ERROR);
            }

            // 保存分片
            saveChunk(file, originalFilename, currentChunk);

            // 检查是否所有分片都已上传
            if (currentChunk == totalChunks - 1) {
                // 所有分片已上传，进行文件合并
                mergeChunks(originalFilename);
            }

            return Result.success("上传成功");
        } catch (IOException e) {
            return Result.error(CodeMsg.SYSTEM_ERROR, "上传失败");
        }
    }

    private boolean checkMD5(MultipartFile file, String expectedMD5) throws IOException {
        try (InputStream is = file.getInputStream()) {
            String md5pwd = DigestUtils.md5DigestAsHex(is);
            System.out.println(md5pwd);
            System.out.println(expectedMD5);
            return expectedMD5.equals(md5pwd);
        }
    }

    private void saveChunk(MultipartFile chunk, String originalFilename, int currentChunk) throws IOException {
        // 创建文件夹用于保存上传的分片
        String pathname = UPLOAD_DIR + PATH_FLAG + originalFilename + SUFFIX;
        File uploadDir = new File(pathname);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 保存分片文件
        Path filePath = Paths.get(pathname, "chunk_" + currentChunk);
        Files.write(filePath, chunk.getBytes());
    }

    private void mergeChunks(String originalFilename) throws IOException {
        log.info("开始合并");
        // 获取所有分片文件
        File[] chunks = new File(UPLOAD_DIR + PATH_FLAG + originalFilename + SUFFIX).listFiles();
        // 排序分片文件
        Arrays.sort(chunks);


        // 合并文件
        Path mergedFilePath = Paths.get(UPLOAD_DIR, originalFilename);
        try (var os = Files.newOutputStream(mergedFilePath, StandardOpenOption.CREATE)) {
            for (File chunk : chunks) {
                byte[] chunkBytes = Files.readAllBytes(chunk.toPath());
                os.write(chunkBytes);
                Files.delete(chunk.toPath()); // 删除已合并的分片
            }
        }
    }


    public String computeMD5(File file) {
        DigestInputStream din = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //第一个参数是一个输入流
            din = new DigestInputStream(new BufferedInputStream(new FileInputStream(file)), md5);

            byte[] b = new byte[1024];
            while (din.read(b) != -1) ;

            byte[] digest = md5.digest();

            String result = file.getName() + ": " +
                    DatatypeConverter.printHexBinary(digest);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (din != null) {
                    din.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
