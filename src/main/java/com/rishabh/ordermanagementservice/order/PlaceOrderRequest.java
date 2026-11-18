package com.rishabh.ordermanagementservice.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
        @NotNull Long user_id,
        @NotBlank String symbol,
        @NotNull  Side side,
        @NotNull @Min(1) Integer quantity
) {


}
