package com.universe.life.auth.service.security;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 集合类Jackson Mixins - 解决OAuth2序列化中集合类型信息格式问题
 *
 * @author 毛伟然
 * @since 2025/11/29
 */
public abstract class CollectionMixins {

    /**
     * Map类型的Mixin - 使用PROPERTY格式的类型信息
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    public abstract static class MapMixin {
    }

    /**
     * List类型的Mixin - 使用PROPERTY格式的类型信息
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    public abstract static class ListMixin {
    }
}