package com.universe.life.trade.privacy.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 订单时间线值对象
 */
@Getter
@EqualsAndHashCode
public class OrderTimeline implements Serializable {

    /**
     * 时间线事件列表
     */
    private final List<TimelineEvent> events;

    private OrderTimeline(List<TimelineEvent> events) {
        this.events = events != null ? Collections.unmodifiableList(events) : Collections.emptyList();
    }

    /**
     * 创建空时间线
     */
    public static OrderTimeline empty() {
        return new OrderTimeline(new ArrayList<>());
    }

    /**
     * 从事件列表创建
     */
    public static OrderTimeline of(List<TimelineEvent> events) {
        return new OrderTimeline(new ArrayList<>(events));
    }

    /**
     * 添加事件
     */
    public OrderTimeline addEvent(String action, String description) {
        List<TimelineEvent> newEvents = new ArrayList<>(this.events);
        newEvents.add(TimelineEvent.of(action, description));
        return new OrderTimeline(newEvents);
    }

    /**
     * 添加事件（指定时间）
     */
    public OrderTimeline addEvent(String action, String description, LocalDateTime time) {
        List<TimelineEvent> newEvents = new ArrayList<>(this.events);
        newEvents.add(TimelineEvent.of(action, description, time));
        return new OrderTimeline(newEvents);
    }

    /**
     * 获取最新事件
     */
    public TimelineEvent getLatestEvent() {
        if (events.isEmpty()) {
            return null;
        }
        return events.get(events.size() - 1);
    }

    /**
     * 获取事件数量
     */
    public int getEventCount() {
        return events.size();
    }

    /**
     * 时间线事件
     */
    @Getter
    @EqualsAndHashCode
    public static class TimelineEvent implements Serializable {
        private final String action;
        private final String description;
        private final LocalDateTime time;

        private TimelineEvent(String action, String description, LocalDateTime time) {
            this.action = action;
            this.description = description;
            this.time = time;
        }

        public static TimelineEvent of(String action, String description) {
            return new TimelineEvent(action, description, LocalDateTime.now());
        }

        public static TimelineEvent of(String action, String description, LocalDateTime time) {
            return new TimelineEvent(action, description, time);
        }

        @Override
        public String toString() {
            return String.format("[%s] %s - %s", time, action, description);
        }
    }

    @Override
    public String toString() {
        return String.format("OrderTimeline{eventCount=%d}", getEventCount());
    }
}
