/**
 * 任务接口层对象转换器
 *
 * <p>
 * 负责将接口层的 Request 对象转换为应用层的 Command/Query 对象。
 * 遵循 DDD 分层架构规范，Controller 层负责 Request 到 Command 的转换。
 * </p>
 *
 * <p>转换器职责：</p>
 * <ul>
 *   <li>Request → Command (写操作)</li>
 *   <li>Request → Query (读操作)</li>
 *   <li>DTO → VO (响应转换)</li>
 * </ul>
 *
 * <p>核心转换器：</p>
 * <ul>
 *   <li>{@link com.universe.life.task.privacy.interfaces.assembler.TaskRequestAssembler} - 请求对象转换</li>
 * </ul>
 *
 * @author Universe Life Team
 * @since 2025-01-18
 */
package com.universe.life.task.privacy.interfaces.assembler;
