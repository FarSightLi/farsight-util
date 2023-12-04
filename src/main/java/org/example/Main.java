package org.example;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    static final int QUEUE_CAPACITY = 100;
    static final Long KEEP_ALIVE_TIME = 1L;



    private AtomicInteger completedTasks = new AtomicInteger(0);
    private AtomicInteger startTasks = new AtomicInteger(0);

    // 添加任务完成的方法
    public void taskCompleted() {
        completedTasks.incrementAndGet();
    }

    public void taskStarted() {
        startTasks.incrementAndGet();
    }

    // 获取已完成任务数的方法
    public int getCompletedTaskCount() {
        return completedTasks.get();
    }

    public int getStartedTaskCount() {
        return startTasks.get();
    }

    public static void main(String[] args) {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();
        Session session = JschUtil.getSession("192.168.1.167", 22, "farsight", "123456");

        ExecutorService executor = main.getExecutor(8);

        System.out.println("size:" + commandMap.size());
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        commandMap.forEach((k, v) -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
                try {
                    main.taskStarted();
                    System.out.println(k + JschUtil.exec(session, v, null, System.err));
                    main.taskCompleted();
                } catch (Exception e) {
                    System.err.println(k);
                    System.err.println(e.getMessage());
                }
            },executor);
            futures.add(future);
        });
        // 等待所有任务完成
        futures.forEach(CompletableFuture::join);

        System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
        System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
        // 检查已完成任务数是否等于预期任务数量
        assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";

        // 线程池执行完毕，关闭
        executor.shutdown();
        JschUtil.close(session);
    }

    public ExecutorService getExecutor(int max) {
        return new ThreadPoolExecutor(
                max,
                max,
                KEEP_ALIVE_TIME,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public HashMap<String, String> getMap() {
        HashMap<String, String> commandMap = new HashMap<>();
        commandMap.put("cpu数量：", "lscpu | awk '/^CPU\\(s\\):/ {print $2}'");
        commandMap.put("系统版本：", "cat /etc/redhat-release");
        commandMap.put("内核版本：", "uname -r");
        commandMap.put("Cpu架构：", "lscpu | awk '/^Architecture:/ {print $2}'");
        commandMap.put("CPU使用率：", "tsar --cpu -C -s util |  awk -F= '{print $2}'");
        commandMap.put("MEM使用量：", "tsar --mem -C -s  used  | awk -F= '{print $2}'");
        commandMap.put("MEM使用率：", "tsar --mem -C -s  util  | awk -F= '{print $2}'");
        commandMap.put("负载：", "tsar --load -C -s load1 |  awk -F= '{print $2}'");
        commandMap.put("磁盘使用率：", "tsar --io -C -s util  |  awk -F= '{print $2}'");
        commandMap.put("接收的字节数：", "cat /proc/net/dev | awk '/ens33:/ {print $2} '");
        commandMap.put("发送的字节数：", "cat /proc/net/dev | awk '/ens33:/ {print $10} '");
        commandMap.put("TCP连接数：", "netstat -nat | grep tcp | wc -l");


        commandMap.put("容器CPU使用率：", "docker stats mrt --no-stream | awk 'NR>1 {print $3} '");
        commandMap.put("容器MEM使用量：", "docker stats mrt --no-stream | awk 'NR>1 {print $4} '");
        commandMap.put("容器MEM分配额：", "docker stats mrt --no-stream | awk 'NR>1 {print $6} '");
        commandMap.put("容器MEM使用率：", "docker stats mrt --no-stream | awk 'NR>1 {print $7} '");
        commandMap.put("容器状态和大小：", "docker ps --format json  --filter 'name=mrt'  --size ");
        commandMap.put("容器磁盘使用量：", "docker system df -v | awk '/mrt/ {print $5}'");
        commandMap.put("容器对应镜像大小：", "docker images `docker system df -v | awk '/mrt/ {print $2}'` | awk 'END{print $NF}'");
        commandMap.put("查看容器版本：", "docker inspect mrt | grep -i version");
        commandMap.put("容器创建时间：", "docker inspect --format '{{.State.StartedAt}}' mrt");
        commandMap.put("容器启动时间：", "docker inspect --format '{{.Created}}' mrt");
        commandMap.put("容器负载核数：", "docker stats mrt --no-stream | awk 'NR>1 {print $3} '");
        commandMap.put("容器负载MEM：", "docker stats mrt --no-stream | awk 'NR>1 {print $4} '");

        return commandMap;
    }



    public void getSysInfo(Session session){

    }
}