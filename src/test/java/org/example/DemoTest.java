package org.example;

import org.example.component.ScheduledTask;
import org.junit.jupiter.api.RepeatedTest;

public class DemoTest {
    @RepeatedTest(1)
    public void test(){
        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.getSysInfoTask();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        scheduledTask.getContainerInfoTask();
    }
}
