package com.universe.life.auth.service.security;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

// 定义一个通用的 Mixin，用于处理自定义的安全类
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class CustomSecurityMixin {
}