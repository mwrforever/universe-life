package com.universe.life.pay.application.service;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.pay.application.service.channel.PayChannelHandler;
import com.universe.life.pay.application.service.channel.PayChannelHandlerRegistry;
import com.universe.life.pay.domain.repository.PayRecordRepository;
import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;
import com.universe.life.pay.model.dto.CreatePaymentDTO;
import com.universe.life.pay.model.dto.CreatePaymentResultDTO;
import com.universe.life.pay.model.enums.PayChannelEnum;
import com.universe.life.pay.model.enums.PayStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PayApplicationService {

    private final PayRecordRepository payRecordRepository;
    private final PayChannelHandlerRegistry handlerRegistry;

    @Transactional(rollbackFor = Exception.class)
    public Result<CreatePaymentResultDTO> createPayment(CreatePaymentDTO dto) {
        if (dto == null) {
            return Result.error("参数不能为空");
        }
        if (dto.getBizType() == null || dto.getBizType().isBlank()) {
            return Result.error("bizType不能为空");
        }
        if (dto.getBizId() == null) {
            return Result.error("bizId不能为空");
        }
        if (dto.getRequestNo() == null || dto.getRequestNo().isBlank()) {
            return Result.error("requestNo不能为空");
        }
        if (dto.getPayerId() == null) {
            return Result.error("payerId不能为空");
        }
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            return Result.error("amount非法");
        }
        if (dto.getChannel() == null) {
            return Result.error("channel不能为空");
        }

        PayRecordPO existed = payRecordRepository.findByRequestNo(dto.getRequestNo()).orElse(null);
        if (existed != null) {
            PayChannelHandler handler = handlerRegistry.getRequired(existed.getChannel());
            PayChannelHandler.ChannelCreateResult channelResult = handler.create(existed);

            if (existed.getChannel() != null && existed.getChannel() == PayChannelEnum.BALANCE.getCode()) {
                String thirdTradeNo = channelResult == null ? null : channelResult.thirdTradeNo();
                if (thirdTradeNo == null || thirdTradeNo.isBlank()) {
                    thirdTradeNo = "BAL-" + existed.getRequestNo();
                }
                payRecordRepository.markSuccess(existed.getId(), thirdTradeNo, existed.getExtra());
                existed.setStatus(PayStatusEnum.SUCCESS.getCode());
                existed.setThirdTradeNo(thirdTradeNo);
            }

            if (channelResult != null) {
                boolean changed = false;
                if ((existed.getThirdTradeNo() == null || existed.getThirdTradeNo().isBlank())
                        && channelResult.thirdTradeNo() != null && !channelResult.thirdTradeNo().isBlank()) {
                    existed.setThirdTradeNo(channelResult.thirdTradeNo());
                    changed = true;
                }
                if ((existed.getThirdPrepayId() == null || existed.getThirdPrepayId().isBlank())
                        && channelResult.thirdPrepayId() != null && !channelResult.thirdPrepayId().isBlank()) {
                    existed.setThirdPrepayId(channelResult.thirdPrepayId());
                    changed = true;
                }
                if (changed) {
                    if (existed.getChannel() == null || existed.getChannel() != PayChannelEnum.BALANCE.getCode()) {
                        existed.setStatus(PayStatusEnum.PAYING.getCode());
                    }
                    existed.setUpdatedAt(LocalDateTime.now());
                    payRecordRepository.save(existed);
                }
            }

            return Result.success(toCreateResult(existed, channelResult == null ? null : channelResult.payParams()));
        }

        LocalDateTime now = LocalDateTime.now();
        PayRecordPO record = new PayRecordPO();
        record.setBizType(dto.getBizType());
        record.setBizId(dto.getBizId());
        record.setRequestNo(dto.getRequestNo());
        record.setPayerId(dto.getPayerId());
        record.setPayeeId(dto.getPayeeId());
        record.setAmount(dto.getAmount());
        record.setChannel(dto.getChannel());
        record.setStatus(PayStatusEnum.CREATED.getCode());
        record.setExtra(dto.getExtra());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        try {
            payRecordRepository.save(record);
        } catch (DuplicateKeyException e) {
            PayRecordPO concurrent = payRecordRepository.findByRequestNo(dto.getRequestNo()).orElse(null);
            if (concurrent != null) {
                PayChannelHandler handler = handlerRegistry.getRequired(concurrent.getChannel());
                PayChannelHandler.ChannelCreateResult channelResult = handler.create(concurrent);

                if (channelResult != null) {
                    boolean changed = false;
                    if ((concurrent.getThirdTradeNo() == null || concurrent.getThirdTradeNo().isBlank())
                            && channelResult.thirdTradeNo() != null && !channelResult.thirdTradeNo().isBlank()) {
                        concurrent.setThirdTradeNo(channelResult.thirdTradeNo());
                        changed = true;
                    }
                    if ((concurrent.getThirdPrepayId() == null || concurrent.getThirdPrepayId().isBlank())
                            && channelResult.thirdPrepayId() != null && !channelResult.thirdPrepayId().isBlank()) {
                        concurrent.setThirdPrepayId(channelResult.thirdPrepayId());
                        changed = true;
                    }
                    if (changed) {
                        concurrent.setStatus(PayStatusEnum.PAYING.getCode());
                        concurrent.setUpdatedAt(LocalDateTime.now());
                        payRecordRepository.save(concurrent);
                    }
                }

                return Result.success(toCreateResult(concurrent, channelResult == null ? null : channelResult.payParams()));
            }
            throw e;
        }

        PayChannelHandler handler = handlerRegistry.getRequired(dto.getChannel());
        PayChannelHandler.ChannelCreateResult channelResult = handler.create(record);

        if (record.getChannel() != null && record.getChannel() == PayChannelEnum.BALANCE.getCode()) {
            String thirdTradeNo = channelResult == null ? null : channelResult.thirdTradeNo();
            if (thirdTradeNo == null || thirdTradeNo.isBlank()) {
                thirdTradeNo = "BAL-" + record.getRequestNo();
            }
            payRecordRepository.markSuccess(record.getId(), thirdTradeNo, record.getExtra());
            record.setStatus(PayStatusEnum.SUCCESS.getCode());
            record.setThirdTradeNo(thirdTradeNo);
        }

        if (channelResult != null) {
            boolean changed = false;
            if (channelResult.thirdTradeNo() != null && !channelResult.thirdTradeNo().isBlank()) {
                record.setThirdTradeNo(channelResult.thirdTradeNo());
                changed = true;
            }
            if (channelResult.thirdPrepayId() != null && !channelResult.thirdPrepayId().isBlank()) {
                record.setThirdPrepayId(channelResult.thirdPrepayId());
                changed = true;
            }
            if (changed) {
                if (record.getChannel() == null || record.getChannel() != PayChannelEnum.BALANCE.getCode()) {
                    record.setStatus(PayStatusEnum.PAYING.getCode());
                }
                record.setUpdatedAt(LocalDateTime.now());
                payRecordRepository.save(record);
            }
        }

        CreatePaymentResultDTO result = toCreateResult(record, channelResult == null ? null : channelResult.payParams());
        return Result.success(result);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> handleCallback(Integer channel, String body, String requestNo, String thirdTradeNo, boolean success) {
        if (requestNo == null || requestNo.isBlank()) {
            return Result.error("requestNo不能为空");
        }

        PayRecordPO record = payRecordRepository.findByRequestNo(requestNo).orElse(null);
        if (record == null) {
            return Result.error(0, "支付记录不存在");
        }

        if (channel != null && record.getChannel() != null && !channel.equals(record.getChannel())) {
            return Result.error("回调渠道不匹配");
        }

        if (record.getStatus() != null && record.getStatus() == PayStatusEnum.SUCCESS.getCode()) {
            return Result.success();
        }

        if (record.getStatus() != null && record.getStatus() == PayStatusEnum.CLOSED.getCode()) {
            return Result.success();
        }

        if (record.getStatus() != null && record.getStatus() == PayStatusEnum.REFUND.getCode()) {
            return Result.success();
        }

        if (success) {
            payRecordRepository.markSuccess(record.getId(), thirdTradeNo, body);
        } else {
            payRecordRepository.markFail(record.getId(), thirdTradeNo, body);
        }

        return Result.success();
    }

    private CreatePaymentResultDTO toCreateResult(PayRecordPO record, String payParams) {
        CreatePaymentResultDTO result = new CreatePaymentResultDTO();
        result.setPayRecordId(record.getId());
        result.setRequestNo(record.getRequestNo());
        result.setChannel(record.getChannel());
        result.setStatus(record.getStatus());
        result.setThirdTradeNo(record.getThirdTradeNo());
        result.setThirdPrepayId(record.getThirdPrepayId());
        result.setPayParams(payParams);
        result.setCreatedAt(record.getCreatedAt());
        return result;
    }
}
