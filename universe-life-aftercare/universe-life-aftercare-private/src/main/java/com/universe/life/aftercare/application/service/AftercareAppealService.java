package com.universe.life.aftercare.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.aftercare.domain.repository.AftercareAppealRepository;
import com.universe.life.aftercare.infrastructure.persistence.po.AftercareAppealPO;
import com.universe.life.aftercare.infrastructure.mq.AftercareEventPublisher;
import com.universe.life.aftercare.interfaces.vo.AftercareAppealVO;
import com.universe.life.aftercare.model.event.AppealCancelledEvent;
import com.universe.life.aftercare.model.event.AppealCreatedEvent;
import com.universe.life.aftercare.model.event.AppealHandledEvent;
import com.universe.life.aftercare.model.dto.AppealSummaryDTO;
import com.universe.life.aftercare.model.dto.CreateAppealDTO;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AftercareAppealService {

    private final AftercareAppealRepository repository;
    private final ObjectMapper objectMapper;
    private final AftercareEventPublisher eventPublisher;

    @Transactional
    public Long createAppeal(CreateAppealDTO dto) {
        if (dto == null) {
            throw new BusinessException.ParamException("参数不能为空");
        }

        if (dto.getOrderId() == null) {
            throw new BusinessException.ParamException("订单ID不能为空");
        }
        if (dto.getTaskId() == null) {
            throw new BusinessException.ParamException("任务ID不能为空");
        }
        if (dto.getAppellantId() == null) {
            throw new BusinessException.ParamException("申诉人ID不能为空");
        }
        if (dto.getAppealType() == null) {
            throw new BusinessException.ParamException("申诉类型不能为空");
        }
        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            throw new BusinessException.ParamException("申诉原因不能为空");
        }

        if (repository.existsPendingByOrderId(dto.getOrderId())) {
            throw new BusinessException.DataAlreadyExistsException("已存在待处理的申诉");
        }

        AftercareAppealPO po = new AftercareAppealPO();
        po.setOrderId(dto.getOrderId());
        po.setTaskId(dto.getTaskId());
        po.setAppellantId(dto.getAppellantId());
        po.setAppealType(dto.getAppealType());
        po.setReason(dto.getReason());
        po.setEvidenceImages(serializeEvidenceImages(dto.getEvidenceImages()));
        po.setStatus(0);

        repository.save(po);

        AppealCreatedEvent event = AppealCreatedEvent.builder()
                .appealId(po.getId())
                .orderId(po.getOrderId())
                .taskId(po.getTaskId())
                .appellantId(po.getAppellantId())
                .appealType(po.getAppealType())
                .createdAt(LocalDateTime.now())
                .build();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publishAppealCreated(event);
                }
            });
        } else {
            eventPublisher.publishAppealCreated(event);
        }

        return po.getId();
    }

    public boolean existsPendingByOrderId(Long orderId) {
        if (orderId == null) {
            return false;
        }
        return repository.existsPendingByOrderId(orderId);
    }

    public AppealSummaryDTO getLatestSummaryByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }

        return repository.findLatestByOrderId(orderId)
                .map(this::toSummaryDTO)
                .orElse(null);
    }

    public PageResult<AftercareAppealVO> pagePendingAppeals(int pageNum, int pageSize) {
        var page = repository.pagePending(pageNum, pageSize);
        List<AftercareAppealVO> records = page.getRecords().stream()
                .map(this::toAppealVO)
                .collect(Collectors.toList());
        return PageResult.of(records, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Transactional
    public void handleAppeal(Long appealId, Long handlerId, Integer result, String handleRemark,
                             Long payableAmountCents, Long paidAmountCents) {
        if (appealId == null) {
            throw new BusinessException.ParamException("申诉ID不能为空");
        }
        if (handlerId == null) {
            throw new BusinessException.ParamException("处理人ID不能为空");
        }
        if (result == null) {
            throw new BusinessException.ParamException("处理结果不能为空");
        }

        AftercareAppealPO po = repository.findById(appealId)
                .orElseThrow(() -> new BusinessException.DataNotFoundException("申诉不存在"));
        if (po.getStatus() != null && po.getStatus() != 0) {
            throw new BusinessException.OperationNotAllowedException("申诉已处理，无法重复处理");
        }
        po.setStatus(1);
        po.setResult(result);
        po.setHandlerId(handlerId);
        po.setHandleRemark(handleRemark);
        po.setHandledAt(LocalDateTime.now());
        repository.save(po);

        AppealHandledEvent event = AppealHandledEvent.builder()
                .appealId(po.getId())
                .orderId(po.getOrderId())
                .taskId(po.getTaskId())
                .appellantId(po.getAppellantId())
                .appealType(po.getAppealType())
                .result(po.getResult())
                .handlerId(po.getHandlerId())
                .payableAmountCents(payableAmountCents)
                .paidAmountCents(paidAmountCents)
                .handledAt(po.getHandledAt())
                .build();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publishAppealHandled(event);
                }
            });
        } else {
            eventPublisher.publishAppealHandled(event);
        }
    }

    @Transactional
    public void cancelAppeal(Long appealId, Long operatorId) {
        if (appealId == null) {
            throw new BusinessException.ParamException("申诉ID不能为空");
        }
        if (operatorId == null) {
            throw new BusinessException.ParamException("操作人ID不能为空");
        }

        AftercareAppealPO po = repository.findById(appealId)
                .orElseThrow(() -> new BusinessException.DataNotFoundException("申诉不存在"));

        if (po.getStatus() == null || po.getStatus() != 0) {
            throw new BusinessException.OperationNotAllowedException("申诉已受理/已处理，无法撤销");
        }
        if (po.getAppellantId() == null || !po.getAppellantId().equals(operatorId)) {
            throw new BusinessException.OperationNotAllowedException("无权撤销该申诉");
        }

        po.setStatus(2);
        repository.save(po);

        AppealCancelledEvent event = AppealCancelledEvent.builder()
                .appealId(po.getId())
                .orderId(po.getOrderId())
                .taskId(po.getTaskId())
                .appellantId(po.getAppellantId())
                .appealType(po.getAppealType())
                .cancelledAt(LocalDateTime.now())
                .build();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publishAppealCancelled(event);
                }
            });
        } else {
            eventPublisher.publishAppealCancelled(event);
        }
    }

    private AftercareAppealVO toAppealVO(AftercareAppealPO po) {
        AftercareAppealVO vo = new AftercareAppealVO();
        vo.setAppealId(po.getId());
        vo.setOrderId(po.getOrderId());
        vo.setTaskId(po.getTaskId());
        vo.setAppellantId(po.getAppellantId());
        vo.setAppealType(po.getAppealType());
        vo.setAppealTypeText(getAppealTypeText(po.getAppealType()));
        vo.setReason(po.getReason());
        vo.setEvidenceImages(deserializeEvidenceImages(po.getEvidenceImages()));
        vo.setStatus(po.getStatus());
        if (po.getStatus() != null && po.getStatus() == 0) {
            vo.setStatusText("待处理");
        } else if (po.getStatus() != null && po.getStatus() == 2) {
            vo.setStatusText("已撤销");
        } else {
            vo.setStatusText("已处理");
        }
        vo.setResult(po.getResult());
        vo.setHandlerId(po.getHandlerId());
        vo.setHandleRemark(po.getHandleRemark());
        vo.setHandleTime(po.getHandledAt());
        vo.setCreatedAt(po.getCreatedAt());
        return vo;
    }

    private AppealSummaryDTO toSummaryDTO(AftercareAppealPO po) {
        AppealSummaryDTO dto = new AppealSummaryDTO();
        dto.setAppealId(po.getId());
        dto.setAppealType(po.getAppealType());
        dto.setAppealTypeText(getAppealTypeText(po.getAppealType()));
        dto.setStatus(po.getStatus());
        if (po.getStatus() != null && po.getStatus() == 0) {
            dto.setStatusText("待处理");
        } else if (po.getStatus() != null && po.getStatus() == 2) {
            dto.setStatusText("已撤销");
        } else {
            dto.setStatusText("已处理");
        }
        dto.setReason(po.getReason());
        dto.setCreatedAt(po.getCreatedAt());
        return dto;
    }

    private String getAppealTypeText(Integer appealType) {
        if (appealType == null) {
            return "";
        }
        return switch (appealType) {
            case 1 -> "成果不符合要求";
            case 2 -> "发布者恶意拒绝";
            case 3 -> "其他";
            default -> "";
        };
    }

    private String serializeEvidenceImages(List<String> evidenceImages) {
        if (evidenceImages == null || evidenceImages.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(evidenceImages);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    private List<String> deserializeEvidenceImages(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
