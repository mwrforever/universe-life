package com.universe.life.common.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class UserAuthInfo implements UserDetails {

    private Long id;

    private List<String> prePermissions;

    private String username;

    private String password;


    public UserAuthInfo(Long id, String username, String password, List<String> prePermissions) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.prePermissions = prePermissions;
    }

    @JsonIgnore
    private List<GrantedAuthority> permissions;


    @Override
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
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
