package com.universe.life.trade.privacy.mapstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * JSON转换工具类
 *
 * @author universe-life
 */
@Slf4j
public class JsonConvertMapstruct {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 字符串列表转JSON
     */
    public static String stringListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("List转JSON失败", e);
            return null;
        }
    }

    /**
     * JSON转字符串列表
     */
    public static List<String> jsonToStringList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON转List失败", e);
            return Collections.emptyList();
        }
    }
}
