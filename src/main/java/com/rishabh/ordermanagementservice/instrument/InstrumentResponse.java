package com.rishabh.ordermanagementservice.instrument;

import java.math.BigDecimal;

public record InstrumentResponse(
        String symbol,
        String name,
        String exchange,
        BigDecimal last_price
) {
    public static InstrumentResponse from(Instrument instrument){
        return new InstrumentResponse(
                instrument.getSymbol(),
                instrument.getName(),
                instrument.getExchange(),
                instrument.getLast_price()
        );
    }
}
