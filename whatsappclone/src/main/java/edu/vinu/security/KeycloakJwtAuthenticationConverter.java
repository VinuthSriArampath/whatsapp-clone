package edu.vinu.security;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt source) {
        return new JwtAuthenticationToken(source,
                Stream.concat(new JwtGrantedAuthoritiesConverter().convert(source).stream(),
                        extractResourceRoles(source).stream()
                ).collect(Collectors.toSet())
        );
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(@NotNull Jwt jwt) {
        var resourceAccess = new HashMap<>(jwt.getClaim("resource_access"));
        var eternal =(Map<String, List<String>>) resourceAccess.get("account");
        var roles = eternal.get("roles");
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE"+role.replace("-","_")))
                .collect(Collectors.toSet());
    }
}
