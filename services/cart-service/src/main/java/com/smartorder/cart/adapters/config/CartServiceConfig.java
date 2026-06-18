package com.smartorder.cart.adapters.config;

import com.smartorder.cart.adapters.persistence.CartRedisAdapter;
import com.smartorder.cart.domain.service.*;
import com.smartorder.common.filter.CorrelationIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

/**
 * Composition root for the Cart Service.
 * Wires all use-case beans to their adapter dependencies.
 */
@Configuration
public class CartServiceConfig {

    @Bean
    public AddToCartService addToCartService(CartRedisAdapter cartRepository) {
        return new AddToCartService(cartRepository);
    }

    @Bean
    public UpdateCartItemService updateCartItemService(CartRedisAdapter cartRepository) {
        return new UpdateCartItemService(cartRepository);
    }

    @Bean
    public RemoveFromCartService removeFromCartService(CartRedisAdapter cartRepository) {
        return new RemoveFromCartService(cartRepository);
    }

    @Bean
    public GetCartService getCartService(CartRedisAdapter cartRepository) {
        return new GetCartService(cartRepository);
    }

    @Bean
    public MergeCartService mergeCartService(CartRedisAdapter cartRepository) {
        return new MergeCartService(cartRepository);
    }

    @Bean
    public ClearCartService clearCartService(CartRedisAdapter cartRepository) {
        return new ClearCartService(cartRepository);
    }

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationFilter() {
        FilterRegistrationBean<CorrelationIdFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new CorrelationIdFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(1);
        return reg;
    }
}