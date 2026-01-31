package com.universe.life.user.privacy.util;

import cn.hutool.core.util.ObjectUtil;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;

/**
 * 验证辅助工具类
 * 提供统一的验证方法，用于业务逻辑验证
 *
 * @author Code Simplifier
 * @since 2025-01-18
 */
public class ValidationHelper {

    private ValidationHelper() {
        // 工具类不允许实例化
    }

    /**
     * 验证对象不为空
     *
     * @param value     待验证的值
     * @param fieldName 字段名称
     * @throws BusinessException.DataNotFoundException 如果值为空
     */
    public static void validateNotNull(Object value, String fieldName) {
        if (ObjectUtil.isNull(value)) {
            throw new BusinessException.DataNotFoundException(
                    ExceptionMessage.Formatter.dataNotFound(fieldName)
            );
        }
    }

    /**
     * 验证实体存在
     *
     * @param exists     是否存在
     * @param entityName 实体名称
     * @throws BusinessException.DataNotFoundException 如果实体不存在
     */
    public static void validateExists(boolean exists, String entityName) {
        if (!exists) {
            throw new BusinessException.DataNotFoundException(
                    ExceptionMessage.Formatter.dataNotFound(entityName)
            );
        }
    }

    /**
     * 验证实体不存在（用于创建时检查重复）
     *
     * @param exists     是否存在
     * @param entityName 实体名称
     * @throws BusinessException.DataAlreadyExistsException 如果实体已存在
     */
    public static void validateNotExists(boolean exists, String entityName) {
        if (exists) {
            throw new BusinessException.DataAlreadyExistsException(
                    ExceptionMessage.Formatter.dataAlreadyExist(entityName)
            );
        }
    }

    /**
     * 验证父实体不是自己
     *
     * @param parentId   父实体ID
     * @param selfId     自身ID
     * @param entityName 实体名称
     * @throws BusinessException.OperationNotAllowedException 如果父实体是自己
     */
    public static void validateParentNotSelf(Long parentId, Long selfId, String entityName) {
        if (ObjectUtil.isNotNull(parentId) && parentId.equals(selfId)) {
            throw new BusinessException.OperationNotAllowedException(
                    ExceptionMessage.Formatter.operationFailed("父" + entityName + "不能是自己")
            );
        }
    }

    /**
     * 验证不是系统实体（系统实体不允许删除）
     *
     * @param isSystem   是否是系统实体
     * @param entityName 实体名称
     * @throws BusinessException.OperationNotAllowedException 如果是系统实体
     */
    public static void validateNotSystemEntity(boolean isSystem, String entityName) {
        if (isSystem) {
            throw new BusinessException.OperationNotAllowedException(
                    ExceptionMessage.Formatter.operationFailed("系统" + entityName + "不允许删除")
            );
        }
    }

    /**
     * 验证没有子实体
     *
     * @param hasChildren 是否有子实体
     * @param entityName  实体名称
     * @throws BusinessException.OperationNotAllowedException 如果有子实体
     */
    public static void validateNoChildren(boolean hasChildren, String entityName) {
        if (hasChildren) {
            throw new BusinessException.OperationNotAllowedException(
                    ExceptionMessage.Formatter.operationFailed("该" + entityName + "下存在子" + entityName + "，不允许删除")
            );
        }
    }

    /**
     * 验证没有关联实体
     *
     * @param count           关联数量
     * @param entityName      实体名称
     * @param associationType 关联类型
     * @throws BusinessException.OperationNotAllowedException 如果有关联实体
     */
    public static void validateNoAssociations(long count, String entityName, String associationType) {
        if (count > 0) {
            throw new BusinessException.OperationNotAllowedException(
                    ExceptionMessage.Formatter.operationFailed(entityName + "已关联" + associationType + "，不允许删除")
            );
        }
    }
}
