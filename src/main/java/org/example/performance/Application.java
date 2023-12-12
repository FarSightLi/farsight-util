package org.example.performance;

import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.scheduled.HostXmlScheduledTask;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = "org.example.performance")
@EnableScheduling
@EnableAspectJAutoProxy
@Slf4j
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    static class ReadXml implements CommandLineRunner {
        @Override
        public void run(String... args) {
            try {
                HostXmlScheduledTask.read();
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
    }
}
