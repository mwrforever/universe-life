package com.universe.life.trade.privacy.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.trade.privacy.domain.model.aggregate.TradeOrderAggregate;
import com.universe.life.trade.privacy.domain.model.valueobject.TradeOrderStatusEnum;
import com.universe.life.trade.privacy.enums.TradeOrderStatus;
import com.universe.life.trade.privacy.infrastructure.persistence.po.TradeOrderPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * 交易订单PO与领域聚合根转换器
 * <p>
 * 使用MapStruct在编译时生成转换代码，避免运行时反射带来的性能损耗。
 * 负责TradeOrderAggregate与TradeOrderPO之间的双向转换。
 * 特别处理JSON字符串与List之间的转换。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class TradeOrderConverter {

    private static final Logger log = LoggerFactory.getLogger(TradeOrderConverter.class);
    
    /** JSON序列化工具 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 领域聚合根转PO
     * <p>
     * 将TradeOrderAggregate转换为TradeOrderPO用于数据库持久化。
     * 处理值对象到基本类型的转换，以及JSON序列化。
     * </p>
     *
     * @param domain 领域聚合根
     * @return 持久化对象
     */
    @Mapping(target = "id", source = "idValue")
    @Mapping(target = "rewardAmount", source = "rewardAmountCents")
    @Mapping(target = "status", source = "status", qualifiedByName = "domainStatusToPO")
    @Mapping(target = "submitContent", expression = "java(domain.hasSubmitResult() ? domain.getSubmitResult().getContent() : null)")
    @Mapping(target = "submitImages", expression = "java(domain.hasSubmitResult() ? serializeImages(domain.getSubmitResult().getImages()) : null)")
    @Mapping(target = "rejectReason", expression = "java(domain.hasRejectInfo() ? domain.getRejectInfo().getReason() : null)")
    @Mapping(target = "deleted", ignore = true)
    public abstract TradeOrderPO toPO(TradeOrderAggregate domain);

    /**
     * PO转领域聚合根
     * <p>
     * 从数据库读取的TradeOrderPO重建为TradeOrderAggregate。
     * 使用聚合根的reconstitute工厂方法确保领域对象的完整性。
     * </p>
     *
     * @param po 持久化对象
     * @return 领域聚合根
     */
    public TradeOrderAggregate toDomain(TradeOrderPO po) {
        if (po == null) {
            return null;
        }
        return TradeOrderAggregate.reconstitute(
                po.getId(),
                po.getTaskId(),
                po.getPublisherId(),
                po.getAcceptorId(),
                po.getRewardAmount(),
                poStatusToDomain(po.getStatus()),
                po.getSubmitContent(),
                parseImages(po.getSubmitImages()),
                po.getRejectReason(),
                null, // rejectedAt - 需要从其他地方获取
                po.getPublisherId(), // rejectedBy - 假设是发布者拒绝
                po.getAppliedAt(),
                po.getApprovedAt(),
                po.getSubmittedAt(),
                po.getCompletedAt(),
                po.getCreatedAt(),
                po.getUpdatedAt(),
                po.getVersion()
        );
    }

    /**
     * 领域状态枚举转PO状态枚举
     *
     * @param domainStatus 领域层状态枚举
     * @return PO层状态枚举
     */
    @Named("domainStatusToPO")
    protected TradeOrderStatus domainStatusToPO(TradeOrderStatusEnum domainStatus) {
        if (domainStatus == null) {
            return null;
        }
        return TradeOrderStatus.of(domainStatus.getCode());
    }

    /**
     * PO状态枚举转领域状态枚举
     *
     * @param poStatus PO层状态枚举
     * @return 领域层状态枚举
     */
    protected TradeOrderStatusEnum poStatusToDomain(TradeOrderStatus poStatus) {
        if (poStatus == null) {
            return null;
        }
        return TradeOrderStatusEnum.of(poStatus.getCode());
    }

    /**
     * 解析图片JSON字符串为列表
     * <p>
     * 将数据库中存储的JSON数组字符串解析为Java List。
     * 解析失败时返回空列表，不抛出异常。
     * </p>
     *
     * @param imagesJson JSON格式的图片URL数组
     * @return 图片URL列表
     */
    protected List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析图片JSON失败: {}", imagesJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * 序列化图片列表为JSON字符串
     * <p>
     * 将Java List序列化为JSON数组字符串用于数据库存储。
     * 序列化失败时返回null。
     * </p>
     *
     * @param images 图片URL列表
     * @return JSON格式的图片URL数组
     */
    protected String serializeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            log.warn("序列化图片列表失败", e);
            return null;
        }
    }
}
