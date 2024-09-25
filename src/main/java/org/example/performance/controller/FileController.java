package org.example.performance.controller;

import lombok.extern.slf4j.Slf4j;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;

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

    @PostMapping("/upload1")
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
            e.printStackTrace();
            return Result.error(CodeMsg.SYSTEM_ERROR, "上传失败");
        }
    }

    @PostMapping("/upload2")
    public Result<?> uploadChunk2(
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
                merge(originalFilename);
            }

            return Result.success("上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SYSTEM_ERROR, "上传失败");
        }
    }

    @PostMapping("/upload3")
    public Result<?> uploadChunk3(
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
                merge1(originalFilename);
            }

            return Result.success("上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SYSTEM_ERROR, "上传失败");
        }
    }

    private boolean checkMD5(MultipartFile file, String expectedMD5) throws IOException {
        try (InputStream is = file.getInputStream()) {
            String md5pwd = DigestUtils.md5DigestAsHex(is);
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

    /**
     * 整个文件读取
     *
     * @param originalFilename
     * @throws IOException
     */
    private void mergeChunks(String originalFilename) throws IOException {
        long a = System.currentTimeMillis();
        log.info("开始合并");
        // 获取所有分片文件
        String tempPathName = UPLOAD_DIR + PATH_FLAG + originalFilename + SUFFIX;
        File[] chunks = new File(tempPathName).listFiles();
        assert chunks != null : tempPathName + "没有文件";
        // 排序分片文件
        Arrays.sort(chunks, Comparator.comparing(file -> Integer.parseInt(file.getName().substring(6))));

        // 合并文件
        Path mergedFilePath = Paths.get(UPLOAD_DIR, originalFilename);
        try (var os = Files.newOutputStream(mergedFilePath, StandardOpenOption.CREATE)) {
            for (File chunk : chunks) {
                byte[] chunkBytes = Files.readAllBytes(chunk.toPath());
                os.write(chunkBytes);
                // 删除已合并的分片
                Files.delete(chunk.toPath());
            }
            Files.delete(Paths.get(tempPathName));
        }
        long b = System.currentTimeMillis();
        log.info("mergeChunks消耗{}ms", b - a);
    }

    private void merge(String originalFilename) throws IOException {
        long a = System.currentTimeMillis();
        String tempPathName = UPLOAD_DIR + PATH_FLAG + originalFilename + SUFFIX;
        // 获取所有分片文件
        File[] chunks = new File(tempPathName).listFiles();
        // 排序分片文件
        assert chunks != null : tempPathName + "没有文件";
        Arrays.sort(chunks, Comparator.comparing(file -> Integer.parseInt(file.getName().substring(6))));
        log.info("开始合并文件");
        Path mergedFilePath = Paths.get(UPLOAD_DIR, originalFilename);

        // 合并文件
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(mergedFilePath, StandardOpenOption.CREATE))) {
            for (File chunk : chunks) {
                try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(chunk.toPath()))) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                }
                Files.delete(chunk.toPath());
            }
        }
        Files.delete(Paths.get(tempPathName));
        long b = System.currentTimeMillis();
        log.info("Try-Try 的merge消耗{}ms", b - a);
    }


    private void merge1(String originalFilename) throws IOException {
        long a = System.currentTimeMillis();
        String tempPathName = UPLOAD_DIR + PATH_FLAG + originalFilename + SUFFIX;
        File[] chunks = new File(tempPathName).listFiles();
        assert chunks != null : tempPathName + "没有文件";
        Arrays.sort(chunks, Comparator.comparing(file -> Integer.parseInt(file.getName().substring(6))));
        //文件名+后缀名
        File file = new File(UPLOAD_DIR + PATH_FLAG + originalFilename);
        if (file.exists()) {
            file.delete();
            log.info("覆盖已经存在的文件");
        }
        BufferedOutputStream destOutputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get(UPLOAD_DIR, originalFilename)));
        for (File chunk : chunks) {
            //循环将每个分片的数据写入目标文件
            //文件读写缓存
            byte[] fileBuffer = new byte[1024];
            //每次读取字节数
            int readBytesLength = 0;
            BufferedInputStream sourceInputStream = new BufferedInputStream(Files.newInputStream(chunk.toPath()));
            while ((readBytesLength = sourceInputStream.read(fileBuffer)) != -1) {
                destOutputStream.write(fileBuffer, 0, readBytesLength);
            }
            Files.delete(chunk.toPath());
            sourceInputStream.close();
            log.info("合并分段文件完成：" + chunk.getName());
        }
        destOutputStream.flush();
        destOutputStream.close();
        Files.delete(Paths.get(tempPathName));
        log.info("合并完成");
        long b = System.currentTimeMillis();
        log.info("merge1消耗{}ms", b - a);
    }

}
