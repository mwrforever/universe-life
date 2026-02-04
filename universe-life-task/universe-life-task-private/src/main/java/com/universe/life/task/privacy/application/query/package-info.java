/**
 * 任务应用层查询对象定义
 *
 * <p>
 * 用于封装应用服务的查询参数，从Controller的Request转换而来。
 * 查询对象代表只读操作，不修改系统状态，遵循CQRS原则。
 * </p>
 *
 * <p>命名规范：{业务}{查询}Query</p>
 *
 * <p>核心Query：</p>
 * <ul>
 *   <li>{@link com.universe.life.task.privacy.application.query.TaskHallQuery} - 任务大厅查询</li>
 *   <li>{@link com.universe.life.task.privacy.application.query.TaskDetailQuery} - 任务详情查询</li>
 *   <li>{@link com.universe.life.task.privacy.application.query.PublishedTasksQuery} - 已发布任务查询</li>
 * </ul>
 *
 * @author Universe Life Team
 * @since 2025-01-18
 */
package com.universe.life.task.privacy.application.query;
