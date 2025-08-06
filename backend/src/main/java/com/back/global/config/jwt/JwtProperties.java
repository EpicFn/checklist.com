package com.back.global.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@Configuration
@ConfigurationProperties(prefix = "custom")
@Getter
@Setter
public class JwtProperties {
    private Jwt jwt;
    private AccessToken accessToken;

    @Getter
    @Setter
    public static class Jwt {
        private String secretKey;
    }

    @Getter
    @Setter
    public static class AccessToken {
        private String expirationSeconds;
    }

    public long getExpirationSecondsAsLong() {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        String expr = accessToken.getExpirationSeconds();
        Integer value = parser.parseExpression(expr).getValue(context, Integer.class);
        return value.longValue();
    }


}
