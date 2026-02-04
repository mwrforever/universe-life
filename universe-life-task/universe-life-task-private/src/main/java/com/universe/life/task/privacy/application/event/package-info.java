/**
 * 任务应用层事件定义
 *
 * <p>
 * 用于封装应用层发布的事件，协调领域事件与外部系统的交互。
 * 应用层负责将领域事件转换为应用事件，并通过事件发布器发送。
 * </p>
 *
 * <p>主要职责：</p>
 * <ul>
 *   <li>监听领域事件</li>
 *   <li>协调跨领域操作</li>
 *   <li>发布集成事件</li>
 *   <li>处理事件补偿</li>
 * </ul>
 *
 * @author Universe Life Team
 * @since 2025-01-18
 */
package com.universe.life.task.privacy.application.event;
