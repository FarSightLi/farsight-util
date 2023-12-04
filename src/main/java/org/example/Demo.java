package org.example;

import org.example.component.ThreadPool;

import java.util.concurrent.ExecutorService;

public class Demo {
    public void getInfo(){
        ExecutorService sysExecutor = ThreadPool.getSys();
        ExecutorService containerExecutor = ThreadPool.getContainer();

    }
}
