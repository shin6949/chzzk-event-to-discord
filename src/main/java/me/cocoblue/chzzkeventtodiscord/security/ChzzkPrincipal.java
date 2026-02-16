package me.cocoblue.chzzkeventtodiscord.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;

public record ChzzkPrincipal(String channelId, AppRole role) implements Principal, Serializable {
    @Override
    public String getName() {
        return channelId;
    }

    public List<? extends GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
