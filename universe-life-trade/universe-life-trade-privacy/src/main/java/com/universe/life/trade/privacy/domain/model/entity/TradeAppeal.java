package com.universe.life.trade.privacy.domain.model.entity;

import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.trade.privacy.enums.TradeAppealStatus;
import com.universe.life.trade.privacy.enums.TradeAppealType;
import com.universe.life.trade.privacy.enums.TradeAppealResult;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易申诉实体
 * <p>
 * 申诉实体代表订单争议的申诉记录。
 * 当订单双方对订单结果存在争议时，可以发起申诉由平台介入处理。
 * </p>
 * <p>
 * 申诉生命周期：
 * <ol>
 *   <li>待处理(PENDING) - 申诉已提交，等待平台处理</li>
 *   <li>已处理(PROCESSED) - 平台已处理完成，给出处理结果</li>
 * </ol>
 * </p>
 * <p>
 * 业务规则：
 * <ul>
 *   <li>每个订单在同一时间只能有一个待处理的申诉</li>
 *   <li>申诉必须提供申诉原因</li>
 *   <li>申诉可以提供证据图片（最多9张）</li>
 *   <li>只有平台管理员可以处理申诉</li>
 * </ul>
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Getter
public class TradeAppeal {

    private Long appealId;
    private Long orderId;
    private Long appellantId;
    private TradeAppealType appealType;
    private String reason;
    private List<String> evidenceImages;
    private TradeAppealStatus status;
    private TradeAppealResult result;
    private Long handlerId;
    private String handleRemark;
    private LocalDateTime handleTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private TradeAppeal() {
    }

    /**
     * 创建新申诉
     *
     * @param orderId        订单ID
     * @param appellantId    申诉人ID
     * @param appealType     申诉类型
     * @param reason         申诉原因
     * @param evidenceImages 证据图片
     * @return 申诉实体
     */
    public static TradeAppeal create(Long orderId, Long appellantId, TradeAppealType appealType,
                                      String reason, List<String> evidenceImages) {
        // 验证参数
        if (orderId == null) {
            throw new BusinessException.ParamException(
                    ExceptionMessage.Formatter.paramRequired("订单ID"));
        }
        if (appellantId == null) {
            throw new BusinessException.ParamException(
                    ExceptionMessage.Formatter.paramRequired("申诉人ID"));
        }
        if (appealType == null) {
            throw new BusinessException.ParamException(
                    ExceptionMessage.Formatter.paramRequired("申诉类型"));
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException.ParamException(
                    ExceptionMessage.Formatter.paramRequired("申诉原因"));
        }
        if (reason.length() < 10 || reason.length() > 500) {
            throw new BusinessException.ParamException(
                    "申诉原因长度必须在10-500字符之间");
        }
        if (evidenceImages != null && evidenceImages.size() > 9) {
            throw new BusinessException.ParamException(
                    "证据图片最多9张");
        }

        TradeAppeal appeal = new TradeAppeal();
        appeal.orderId = orderId;
        appeal.appellantId = appellantId;
        appeal.appealType = appealType;
        appeal.reason = reason;
        appeal.evidenceImages = evidenceImages;
        appeal.status = TradeAppealStatus.PENDING;
        appeal.createdAt = LocalDateTime.now();
        appeal.updatedAt = LocalDateTime.now();
        return appeal;
    }

    /**
     * 从持久化数据重建
     */
    public static TradeAppeal reconstitute(Long appealId, Long orderId, Long appellantId,
                                            TradeAppealType appealType, String reason, List<String> evidenceImages,
                                            TradeAppealStatus status, TradeAppealResult result,
                                            Long handlerId, String handleRemark, LocalDateTime handleTime,
                                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        TradeAppeal appeal = new TradeAppeal();
        appeal.appealId = appealId;
        appeal.orderId = orderId;
        appeal.appellantId = appellantId;
        appeal.appealType = appealType;
        appeal.reason = reason;
        appeal.evidenceImages = evidenceImages;
        appeal.status = status;
        appeal.result = result;
        appeal.handlerId = handlerId;
        appeal.handleRemark = handleRemark;
        appeal.handleTime = handleTime;
        appeal.createdAt = createdAt;
        appeal.updatedAt = updatedAt;
        return appeal;
    }

    /**
     * 设置ID（创建后由仓储设置）
     */
    public void setAppealId(Long appealId) {
        this.appealId = appealId;
    }

    /**
     * 处理申诉
     *
     * @param handlerId    处理人ID
     * @param result       处理结果
     * @param handleRemark 处理备注
     */
    public void handle(Long handlerId, TradeAppealResult result, String handleRemark) {
        if (!status.isPending()) {
            throw new BusinessException.OperationNotAllowedException(
                    "申诉已处理，无法重复处理");
        }
        if (handlerId == null) {
            throw new BusinessException.ParamException(
                    ExceptionMessage.Formatter.paramRequired("处理人ID"));
        }
        if (result == null) {
            throw new BusinessException.ParamException(
                    ExceptionMessage.Formatter.paramRequired("处理结果"));
        }

        this.status = TradeAppealStatus.PROCESSED;
        this.result = result;
        this.handlerId = handlerId;
        this.handleRemark = handleRemark;
        this.handleTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 判断是否已处理
     *
     * @return 如果已处理返回true
     */
    public boolean isProcessed() {
        return status == TradeAppealStatus.PROCESSED;
    }

    /**
     * 判断是否支持申诉方
     *
     * @return 如果支持申诉方返回true
     */
    public boolean isSupported() {
        return result != null && result.isSupport();
    }

    /**
     * 判断申诉是否被驳回
     *
     * @return 如果被驳回返回true
     */
    public boolean isRejected() {
        return result != null && !result.isSupport();
    }
}
