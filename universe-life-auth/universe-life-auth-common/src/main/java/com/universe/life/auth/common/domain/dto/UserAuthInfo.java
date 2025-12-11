package com.universe.life.auth.common.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * @author 毛伟然
 * @since 2025/11/4 10:03
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class UserAuthInfo implements UserDetails {

    private Long id;

    private List<String> prePermissions;

    private String avatar;

    private String username;

    private String password;

    @JsonCreator
    public UserAuthInfo(
            @JsonProperty("id") Long id,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("prePermissions") List<String> prePermissions,
            @JsonProperty("avatar") String avatar
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.prePermissions = prePermissions;
        this.avatar = avatar;
    }
    @JsonIgnore
    private List<GrantedAuthority> permissions;


    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions != null) {
            return permissions;
        }
        return prePermissions == null ? List.of() : prePermissions.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
