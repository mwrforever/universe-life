package com.universe.life.task.privacy.application.assembler.support;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class TaskFormatSupport {

    private TaskFormatSupport() {
    }

    public static String formatAmountToYuan(Long amountInCents) {
        if (amountInCents == null) {
            return "0.00";
        }
        BigDecimal yuan = BigDecimal.valueOf(amountInCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return yuan.toString();
    }

    public static String formatFriendlyTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(now, dateTime);
        long hours = ChronoUnit.HOURS.between(now, dateTime);
        long minutes = ChronoUnit.MINUTES.between(now, dateTime);

        if (days > 0) {
            return days + "天后";
        } else if (hours > 0) {
            return hours + "小时后";
        } else if (minutes > 0) {
            return minutes + "分钟后";
        } else if (minutes < 0) {
            long pastDays = Math.abs(ChronoUnit.DAYS.between(dateTime, now));
            if (pastDays > 0) {
                return pastDays + "天前";
            }
            long pastHours = Math.abs(ChronoUnit.HOURS.between(dateTime, now));
            if (pastHours > 0) {
                return pastHours + "小时前";
            }
            long pastMinutes = Math.abs(ChronoUnit.MINUTES.between(dateTime, now));
            return pastMinutes + "分钟前";
        } else {
            return "刚刚";
        }
    }

    public static String formatAcceptorProgress(Integer current, Integer max) {
        if (current == null || max == null) {
            return "0/0";
        }
        return current + "/" + max;
    }
}
