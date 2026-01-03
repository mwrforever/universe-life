package com.universe.life.model.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminUserInfoDTO {

    private Long id;

    private List<String> permissions;

    private String username;

    private String password;

    private String avatar;

}
