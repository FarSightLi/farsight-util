package org.example;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.Session;
import org.example.component.SshSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MainTest {

    private static Session session;

    private int sleepTime = 10;


    @BeforeAll
    public static void setup() {
        // 创建并初始化 Session 对象
        session = JschUtil.getSession("192.168.1.167", 22, "farsight", "123456");
    }


    @RepeatedTest(10)
    public void test1() {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();

        ExecutorService executor = main.getExecutor(7);

        System.out.println("size:" + commandMap.size());
        List<Future<?>> futures = new ArrayList<>();

        // 提交任务
        commandMap.forEach((k, v) -> {
            Future<?> submit = executor.submit(new CommandTask(k, v, session, main));
            futures.add(submit);
        });

        // 等待所有任务完成
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {

            System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
            System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
            // 检查已完成任务数是否等于预期任务数量
            assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";
        }

        // 线程池执行完毕，关闭
        executor.shutdown();
    }

    @RepeatedTest(10)
    public void test2() {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();

        ExecutorService executor = main.getExecutor(8);

        System.out.println("size:" + commandMap.size());
        List<Future<?>> futures = new ArrayList<>();

        // 提交任务
        commandMap.forEach((k, v) -> {
            Future<?> submit = executor.submit(new CommandTask(k, v, session, main));
            futures.add(submit);
        });

        // 等待所有任务完成
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {

            System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
            System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
            // 检查已完成任务数是否等于预期任务数量
            assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";
        }

        // 线程池执行完毕，关闭
        executor.shutdown();
    }

    @RepeatedTest(10)
    public void test3() {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();

        ExecutorService executor = main.getExecutor(9);

        System.out.println("size:" + commandMap.size());
        List<Future<?>> futures = new ArrayList<>();

        // 提交任务
        commandMap.forEach((k, v) -> {
            Future<?> submit = executor.submit(new CommandTask(k, v, session, main));
            futures.add(submit);
        });

        // 等待所有任务完成
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {

            System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
            System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
            // 检查已完成任务数是否等于预期任务数量
            assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";
        }

        // 线程池执行完毕，关闭
        executor.shutdown();
    }

    @RepeatedTest(10)
    public void test4() {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();

        ExecutorService executor = main.getExecutor(10);

        System.out.println("size:" + commandMap.size());
        List<Future<?>> futures = new ArrayList<>();

        // 提交任务
        commandMap.forEach((k, v) -> {
            Future<?> submit = executor.submit(new CommandTask(k, v, session, main));
            futures.add(submit);
        });

        // 等待所有任务完成
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {

            System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
            System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
            // 检查已完成任务数是否等于预期任务数量
            assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";
        }

        // 线程池执行完毕，关闭
        executor.shutdown();
    }

    @RepeatedTest(10)
    public void testWait1() {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();

        ExecutorService executor = main.getExecutor(7);

        System.out.println("size:" + commandMap.size());
        List<Future<?>> futures = new ArrayList<>();

        // 提交任务
        commandMap.forEach((k, v) -> {
            Future<?> submit = executor.submit(new CommandTask(k, v, session, main));
            futures.add(submit);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 等待所有任务完成
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {

            System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
            System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
            // 检查已完成任务数是否等于预期任务数量
            assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";
        }

        // 线程池执行完毕，关闭
        executor.shutdown();
    }

    @RepeatedTest(10)
    public void testWait2() {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();

        ExecutorService executor = main.getExecutor(8);

        System.out.println("size:" + commandMap.size());
        List<Future<?>> futures = new ArrayList<>();

        // 提交任务
        commandMap.forEach((k, v) -> {
            Future<?> submit = executor.submit(new CommandTask(k, v, session, main));
            futures.add(submit);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 等待所有任务完成
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {

            System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
            System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
            // 检查已完成任务数是否等于预期任务数量
            assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";
        }

        // 线程池执行完毕，关闭
        executor.shutdown();
    }

    @RepeatedTest(10)
    public void testWait3() {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();

        ExecutorService executor = main.getExecutor(9);

        System.out.println("size:" + commandMap.size());
        List<Future<?>> futures = new ArrayList<>();

        // 提交任务
        commandMap.forEach((k, v) -> {
            Future<?> submit = executor.submit(new CommandTask(k, v, session, main));
            futures.add(submit);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 等待所有任务完成
        try {
            for (Future<?> future : futures) {
                future.get();
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {

            System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
            System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
            // 检查已完成任务数是否等于预期任务数量
            assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";
        }

        // 线程池执行完毕，关闭
        executor.shutdown();
    }

    @RepeatedTest(10)
    public void testWait4() {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();

        ExecutorService executor = main.getExecutor(10);

        System.out.println("size:" + commandMap.size());
        List<Future<?>> futures = new ArrayList<>();

        // 提交任务
        commandMap.forEach((k, v) -> {
            Future<?> submit = executor.submit(new CommandTask(k, v, session, main));
            futures.add(submit);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 等待所有任务完成
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {

            System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
            System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
            // 检查已完成任务数是否等于预期任务数量
            assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";
        }

        // 线程池执行完毕，关闭
        executor.shutdown();
    }

    @RepeatedTest(10)
    public void testWait5() {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();

        ExecutorService executor = main.getExecutor(11);

        System.out.println("size:" + commandMap.size());
        List<Future<?>> futures = new ArrayList<>();

        // 提交任务
        commandMap.forEach((k, v) -> {
            Future<?> submit = executor.submit(new CommandTask(k, v, session, main));
            futures.add(submit);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 等待所有任务完成
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {

            System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
            System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
            // 检查已完成任务数是否等于预期任务数量
            assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";
        }

        // 线程池执行完毕，关闭
        executor.shutdown();
    }

    @RepeatedTest(10)
    public void testWait6() {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();

        ExecutorService executor = main.getExecutor(Runtime.getRuntime().availableProcessors());

        System.out.println("size:" + commandMap.size());
        List<Future<?>> futures = new ArrayList<>();

        // 提交任务
        commandMap.forEach((k, v) -> {
            Future<?> submit = executor.submit(new CommandTask(k, v, session, main));
            futures.add(submit);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 等待所有任务完成
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {

            System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
            System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
            // 检查已完成任务数是否等于预期任务数量
            assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";
        }

        // 线程池执行完毕，关闭
        executor.shutdown();
    }

    @RepeatedTest(10)
    public void testWait7() {
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();

        ExecutorService executor = main.getExecutor(Runtime.getRuntime().availableProcessors() * 2);

        System.out.println("size:" + commandMap.size());
        List<Future<?>> futures = new ArrayList<>();

        // 提交任务
        commandMap.forEach((k, v) -> {
            Future<?> submit = executor.submit(new CommandTask(k, v, session, main));
            futures.add(submit);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 等待所有任务完成
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {

            System.out.println("开始了" + main.getStartedTaskCount() + "个任务");
            System.out.println("执行了" + main.getCompletedTaskCount() + "个任务");
            // 检查已完成任务数是否等于预期任务数量
            assert commandMap.size() == main.getCompletedTaskCount() : "有任务没执行,执行了" + main.getCompletedTaskCount() + "个任务";
        }

        // 线程池执行完毕，关闭
        executor.shutdown();
    }


    @RepeatedTest(100)
    public void ascTest(){
        Main main = new Main();
        HashMap<String, String> commandMap = main.getMap();
        Session session = SshSession.getSession();

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
    }

}
