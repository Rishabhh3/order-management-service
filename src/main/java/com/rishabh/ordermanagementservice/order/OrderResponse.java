package com.rishabh.ordermanagementservice.order;

import java.math.BigDecimal;

public record OrderResponse (
        Long orderId,
        String symbol,
        Side side,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalValue,
        String status
){
}
