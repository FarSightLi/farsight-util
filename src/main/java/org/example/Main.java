package org.example;

import cn.hutool.extra.ssh.JschUtil;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors(); // 核心线程数
    static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;

    static final int QUEUE_CAPACITY = 100; //队列大小
    static final Long KEEP_ALIVE_TIME = 1L; // 存活时间
    static final com.jcraft.jsch.Session session = JschUtil.getSession("192.168.1.167", 22, "farsight", "123456");

    public static void main(String[] args) {
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


        commandMap.put("磁盘使用率：", "tsar --io -C -s util  |  awk -F= '{print $2}'");


        ExecutorService executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());

        commandMap.forEach((k,v) -> {
            executor.submit(new CommandTask(k,v));
        });

        // 关闭线程池
        executor.shutdown();

    }

    private static class CommandTask implements Runnable {
        private String command;
        private String info;

        public CommandTask(String info, String command) {
            this.command = command;
            this.info = info;
        }

        @Override
        public void run() {
            System.out.println(info+JschUtil.exec(session, command, null));
        }
    }

}