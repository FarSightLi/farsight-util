package org.example.performance.component;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.Session;

public class CommandTask implements Runnable {
    private final String command;
    private final String info;
    private final Session session;

    private Main main;


    public CommandTask(String info, String command, Session session, Main main) {
        this.command = command;
        this.info = info;
        this.session = session;
        this.main = main;
    }

    @Override
    public void run() {
        try {
            main.taskStarted();
            System.out.println(info + JschUtil.exec(session, command, null, System.err));
            System.out.println(info + "完毕");
            // 在任务完成时调用 Main 类的 taskCompleted 方法
            main.taskCompleted();
        } catch (Exception e) {
            System.err.println(info);
            System.err.println(e.getMessage());
        }

    }
}
