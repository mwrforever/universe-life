package com.universe.life.task.privacy.mapstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * JSON转换工具类，供MapStruct使用
 */
@Component
public class JsonConvertMapstruct {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Named("jsonToStringList")
    public static List<String> jsonToStringList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    @Named("stringListToJson")
    public static String stringListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
