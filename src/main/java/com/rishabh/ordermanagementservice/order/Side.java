package com.rishabh.ordermanagementservice.order;

public enum Side {
    // With an enum, anything else fails to compile, and Spring rejects a bad value in the JSON before your code runs.
    BUY,
    SELL
}
