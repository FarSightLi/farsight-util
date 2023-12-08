package org.example.performance.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 数据转换工具类
 * @date 2023/12/8 16:51:58
 */
public class DataUtil {
    private DataUtil() {
    }

    public static BigDecimal mb2Gb(BigDecimal mb) {
        return mb.divide(BigDecimal.valueOf(1024), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal string2Decimal(String value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 移除百分号
     *
     * @param target
     * @return
     */
    public static String removePercent(String target) {
        return target.replace("%", "").trim();
    }

    public static LocalDateTime getTime(String stringTime) {
        return LocalDateTime.parse(stringTime, DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * 根据传进的字符自动解析为MB大小
     *
     * @param dataSizeString
     * @return
     */
    public static BigDecimal parseDataSize(String dataSizeString) {
        Pattern pattern = Pattern.compile("(\\d+\\.?\\d*)([BKMGTPEZYbkmgtpezy]B?)");
        Matcher matcher = pattern.matcher(dataSizeString);

        if (matcher.find()) {
            String valueStr = matcher.group(1);
            String unit = matcher.group(2).toUpperCase();
            BigDecimal value = new BigDecimal(valueStr);

            switch (unit) {
                case "B":
                    return value.divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);
                case "KB":
                case "K":
                    return value.divide(BigDecimal.valueOf(1024), 2, RoundingMode.HALF_UP);
                case "MB":
                case "MIB":
                case "M":
                    return value;
                case "GB":
                case "G":
                case "GIB":
                    return value.multiply(BigDecimal.valueOf(1024));
                default:
                    throw new IllegalArgumentException("Unsupported unit: " + unit);
            }
        } else {
            return new BigDecimal(dataSizeString).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);
        }
    }
}
