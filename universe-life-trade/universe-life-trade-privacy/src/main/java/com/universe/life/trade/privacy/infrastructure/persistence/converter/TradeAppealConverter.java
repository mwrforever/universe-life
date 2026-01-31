package com.universe.life.trade.privacy.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.trade.privacy.infrastructure.persistence.po.TradeAppealPO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * 交易申诉PO转换器
 * <p>
 * 使用MapStruct在编译时生成转换代码，避免运行时反射带来的性能损耗。
 * 负责TradeAppealPO的数据转换，特别是JSON字符串与List之间的转换。
 * </p>
 * 
 * <p>注意：当前申诉功能暂未实现完整的领域模型（Aggregate），
 * 此Converter主要提供JSON解析等辅助方法供Repository使用。</p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class TradeAppealConverter {

    private static final Logger log = LoggerFactory.getLogger(TradeAppealConverter.class);
    
    /** JSON序列化工具 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 解析证据图片JSON字符串为列表
     * <p>
     * 将数据库中存储的JSON数组字符串解析为Java List。
     * 解析失败时返回空列表，不抛出异常。
     * </p>
     *
     * @param imagesJson JSON格式的图片URL数组
     * @return 图片URL列表
     */
    @Named("parseEvidenceImages")
    public List<String> parseEvidenceImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析证据图片JSON失败: {}", imagesJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * 序列化证据图片列表为JSON字符串
     * <p>
     * 将Java List序列化为JSON数组字符串用于数据库存储。
     * 序列化失败时返回null。
     * </p>
     *
     * @param images 图片URL列表
     * @return JSON格式的图片URL数组
     */
    @Named("serializeEvidenceImages")
    public String serializeEvidenceImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            log.warn("序列化证据图片列表失败", e);
            return null;
        }
    }
    
    /**
     * 复制PO对象
     * <p>
     * 创建TradeAppealPO的副本，用于更新操作前的数据隔离。
     * </p>
     *
     * @param source 源PO对象
     * @return PO对象副本
     */
    public TradeAppealPO copy(TradeAppealPO source) {
        if (source == null) {
            return null;
        }
        TradeAppealPO target = new TradeAppealPO();
        target.setId(source.getId());
        target.setOrderId(source.getOrderId());
        target.setAppellantId(source.getAppellantId());
        target.setAppealType(source.getAppealType());
        target.setReason(source.getReason());
        target.setEvidenceImages(source.getEvidenceImages());
        target.setStatus(source.getStatus());
        target.setResult(source.getResult());
        target.setHandlerId(source.getHandlerId());
        target.setHandleRemark(source.getHandleRemark());
        target.setHandledAt(source.getHandledAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        target.setDeleted(source.getDeleted());
        return target;
    }
}
