package org.example.performance.util;

import org.example.performance.component.HasUpdateTime;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 不便归类的工具类
 * @date 2023/12/14 09:30:35
 */
@Component
public class MyUtil {
    public static Integer getInterval(LocalDateTime startTime, LocalDateTime endTime) {
        // 指定时间间隔（秒）
        int timeIntervalInSeconds;
        Duration between = Duration.between(startTime, endTime);
        if (between.toDays() >= 1) {
            timeIntervalInSeconds = 60 * 60;
        } else if (between.toHours() >= 12) {
            timeIntervalInSeconds = 60;
        } else {
            timeIntervalInSeconds = 1;
        }
        return timeIntervalInSeconds;
    }

    /**
     * 根据时间间隔获得每个区间内最新的数据，排序后进行返回
     * <p>
     * 要求该列表必须实现了HasUpdateTime接口
     *
     * @param oldList               未筛选前的列表
     * @param timeIntervalInSeconds 时间间隔（以秒为单位）
     * @return 筛选后的列表
     */
    public static <T extends HasUpdateTime> List<T> filterListByTime(List<T> oldList, int timeIntervalInSeconds) {
        Map<Long, List<T>> hourlyMetricsMap = oldList.stream()
                .collect(Collectors.groupingBy(
                        metrics -> metrics.getUpdateTime().atZone(ZoneOffset.ofHours(8)).toEpochSecond() / timeIntervalInSeconds
                ));
        return hourlyMetricsMap.values().stream()
                .map(metricsList -> metricsList.stream()
                        .max(Comparator.comparing(HasUpdateTime::getUpdateTime))
                        .orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static boolean validTime(LocalDateTime startTime, LocalDateTime endTime) {
        return endTime.isBefore(startTime);
    }
}
