package com.universe.life.trade.privacy.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.common.domain.PageResult;
import com.universe.life.trade.privacy.domain.model.entity.TradeAppeal;
import com.universe.life.trade.privacy.domain.repository.TradeAppealRepository;
import com.universe.life.trade.privacy.domain.repository.TradeOrderRepository;
import com.universe.life.trade.privacy.enums.TradeAppealResult;
import com.universe.life.trade.privacy.enums.TradeAppealType;
import com.universe.life.trade.privacy.infrastructure.persistence.po.TradeAppealPO;
import com.universe.life.trade.privacy.interfaces.vo.TradeAppealVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易申诉应用服务
 * <p>
 * 负责申诉相关的业务用例编排，包括：
 * <ul>
 *   <li>发起申诉 - 用户对订单结果发起申诉</li>
 *   <li>处理申诉 - 管理员处理申诉</li>
 *   <li>查询申诉 - 申诉列表查询</li>
 * </ul>
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeAppealService {

    /** 申诉仓储 */
    private final TradeAppealRepository appealRepository;
    
    /** 订单仓储 */
    private final TradeOrderRepository orderRepository;
    
    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 发起申诉
     * <p>
     * 业务流程：
     * <ol>
     *   <li>验证订单是否存在</li>
     *   <li>验证用户是否有权限发起申诉</li>
     *   <li>检查是否已存在待处理的申诉</li>
     *   <li>创建申诉实体</li>
     *   <li>持久化申诉数据</li>
     * </ol>
     * </p>
     *
     * @param orderId        订单ID
     * @param appellantId    申诉人ID
     * @param appealType     申诉类型
     * @param reason         申诉原因
     * @param evidenceImages 证据图片
     * @return 申诉ID
     * @throws BusinessException.DataNotFoundException 当订单不存在时抛出
     * @throws BusinessException.OperationNotAllowedException 当无权操作或已存在待处理申诉时抛出
     */
    @Transactional
    public Long initiateAppeal(Long orderId, Long appellantId, TradeAppealType appealType,
                               String reason, List<String> evidenceImages) {
        // 1. 验证订单是否存在
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException.DataNotFoundException(
                        ExceptionMessage.Formatter.dataNotFound("订单")));

        // 2. 验证用户是否有权限发起申诉（发布者或接单者）
        if (!order.canOperate(appellantId)) {
            throw new BusinessException.OperationNotAllowedException("无权对此订单发起申诉");
        }

        // 3. 检查是否已存在待处理的申诉
        if (appealRepository.existsPendingByOrderId(orderId)) {
            throw new BusinessException.DataAlreadyExistsException("已存在待处理的申诉");
        }

        // 4. 创建申诉实体
        TradeAppeal appeal = TradeAppeal.create(orderId, appellantId, appealType, reason, evidenceImages);

        // 5. 转换为PO并持久化
        TradeAppealPO appealPO = toAppealPO(appeal);
        appealPO = appealRepository.save(appealPO);

        log.info("发起申诉成功: appealId={}, orderId={}, appellantId={}", 
                appealPO.getId(), orderId, appellantId);
        return appealPO.getId();
    }

    /**
     * 处理申诉
     * <p>
     * 管理员处理申诉，给出处理结果和备注。
     * </p>
     *
     * @param appealId     申诉ID
     * @param handlerId    处理人ID
     * @param result       处理结果
     * @param handleRemark 处理备注
     * @throws BusinessException.DataNotFoundException 当申诉不存在时抛出
     * @throws BusinessException.OperationNotAllowedException 当申诉状态不允许处理时抛出
     */
    @Transactional
    public void handleAppeal(Long appealId, Long handlerId, TradeAppealResult result, String handleRemark) {
        // 1. 查询申诉
        TradeAppealPO appealPO = appealRepository.findById(appealId)
                .orElseThrow(() -> new BusinessException.DataNotFoundException(
                        ExceptionMessage.Formatter.dataNotFound("申诉")));

        // 2. 转换为实体
        TradeAppeal appeal = toAppealEntity(appealPO);

        // 3. 处理申诉
        appeal.handle(handlerId, result, handleRemark);

        // 4. 转换为PO并保存
        TradeAppealPO updatedPO = toAppealPO(appeal);
        updatedPO.setId(appealId); // 保持ID不变
        appealRepository.save(updatedPO);

        log.info("处理申诉成功: appealId={}, handlerId={}, result={}", appealId, handlerId, result);
    }

    /**
     * 分页查询待处理申诉列表
     * <p>
     * 供管理员查看和处理申诉。
     * </p>
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public PageResult<TradeAppealVO> pagePendingAppeals(int pageNum, int pageSize) {
        // 1. 查询申诉列表
        List<TradeAppealPO> appeals = appealRepository.findPendingAppeals(pageNum, pageSize);

        // 2. 统计总数
        long total = appealRepository.countPendingAppeals();

        // 3. 转换为VO
        List<TradeAppealVO> voList = appeals.stream()
                .map(this::toAppealVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, total, pageNum, pageSize);
    }

    /**
     * 根据订单ID查询申诉列表
     *
     * @param orderId 订单ID
     * @return 申诉列表
     */
    public List<TradeAppealVO> listAppealsByOrderId(Long orderId) {
        List<TradeAppealPO> appeals = appealRepository.findByOrderId(orderId);
        return appeals.stream()
                .map(this::toAppealVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据订单ID查询最新申诉
     *
     * @param orderId 订单ID
     * @return 最新申诉VO，如果不存在则返回null
     */
    public TradeAppealVO getLatestAppealByOrderId(Long orderId) {
        return appealRepository.findLatestByOrderId(orderId)
                .map(this::toAppealVO)
                .orElse(null);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将申诉实体转换为PO
     *
     * @param appeal 申诉实体
     * @return 申诉PO
     */
    private TradeAppealPO toAppealPO(TradeAppeal appeal) {
        TradeAppealPO po = new TradeAppealPO();
        po.setOrderId(appeal.getOrderId());
        po.setAppellantId(appeal.getAppellantId());
        po.setAppealType(appeal.getAppealType());
        po.setReason(appeal.getReason());
        po.setEvidenceImages(serializeEvidenceImages(appeal.getEvidenceImages()));
        po.setStatus(appeal.getStatus());
        po.setResult(appeal.getResult());
        po.setHandlerId(appeal.getHandlerId());
        po.setHandleRemark(appeal.getHandleRemark());
        po.setHandledAt(appeal.getHandleTime());
        return po;
    }

    /**
     * 将申诉PO转换为实体
     *
     * @param po 申诉PO
     * @return 申诉实体
     */
    private TradeAppeal toAppealEntity(TradeAppealPO po) {
        return TradeAppeal.reconstitute(
                po.getId(),
                po.getOrderId(),
                po.getAppellantId(),
                po.getAppealType(),
                po.getReason(),
                deserializeEvidenceImages(po.getEvidenceImages()),
                po.getStatus(),
                po.getResult(),
                po.getHandlerId(),
                po.getHandleRemark(),
                po.getHandledAt(),
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }

    /**
     * 将申诉PO转换为VO
     *
     * @param po 申诉PO
     * @return 申诉VO
     */
    private TradeAppealVO toAppealVO(TradeAppealPO po) {
        TradeAppealVO vo = new TradeAppealVO();
        vo.setAppealId(po.getId());
        vo.setOrderId(po.getOrderId());
        vo.setAppellantId(po.getAppellantId());
        vo.setAppealType(po.getAppealType() != null ? po.getAppealType().getValue() : null);
        vo.setAppealTypeText(po.getAppealType() != null ? po.getAppealType().getDesc() : "");
        vo.setReason(po.getReason());
        vo.setEvidenceImages(deserializeEvidenceImages(po.getEvidenceImages()));
        vo.setStatus(po.getStatus() != null ? po.getStatus().getValue() : null);
        vo.setStatusText(po.getStatus() != null ? po.getStatus().getDesc() : "");
        vo.setResult(po.getResult() != null ? po.getResult().getValue() : null);
        vo.setResultText(po.getResult() != null ? po.getResult().getDesc() : "");
        vo.setHandlerId(po.getHandlerId());
        vo.setHandleRemark(po.getHandleRemark());
        vo.setHandleTime(po.getHandledAt());
        vo.setCreatedAt(po.getCreatedAt());
        return vo;
    }

    /**
     * 序列化证据图片列表为JSON字符串
     *
     * @param evidenceImages 证据图片列表
     * @return JSON字符串
     */
    private String serializeEvidenceImages(List<String> evidenceImages) {
        if (evidenceImages == null || evidenceImages.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(evidenceImages);
        } catch (JsonProcessingException e) {
            log.error("序列化证据图片失败", e);
            return null;
        }
    }

    /**
     * 反序列化JSON字符串为证据图片列表
     *
     * @param evidenceImagesJson JSON字符串
     * @return 证据图片列表
     */
    private List<String> deserializeEvidenceImages(String evidenceImagesJson) {
        if (evidenceImagesJson == null || evidenceImagesJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(evidenceImagesJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("反序列化证据图片失败", e);
            return Collections.emptyList();
        }
    }
}
