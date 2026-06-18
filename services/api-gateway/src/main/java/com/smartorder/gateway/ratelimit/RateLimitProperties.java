package com.smartorder.gateway.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "smartorder.gateway.rate-limit")
public class RateLimitProperties {
    private int replenishRate     = 20;
    private int burstCapacity     = 40;
    private int authReplenishRate = 5;
    private int authBurstCapacity = 10;
}