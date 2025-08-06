package com.back.global.security;

import com.back.global.enums.MemberType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

// member에 맞춰서 수정 예정
public class SecurityUser extends User {
    @Getter
    private final Long id;
    @Getter
    private final String password;
    @Getter
    private final String nickname;
    @Getter
    private final String tag;
    @Getter
    private final MemberType memberType;

    public SecurityUser(
            Long id,
            String nickname,
            String tag,
            MemberType memberType,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(nickname, password, authorities);
        this.id = id;
        this.nickname = nickname;
        this.tag = tag;
        this.password = password;
        this.memberType = memberType;
    }
}
