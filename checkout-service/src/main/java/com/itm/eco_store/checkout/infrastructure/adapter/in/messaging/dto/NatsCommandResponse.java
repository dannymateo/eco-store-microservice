package com.itm.eco_store.checkout.infrastructure.adapter.in.messaging.dto;

public record NatsCommandResponse(
        boolean success,
        Object data,
        String error
) {
    public static NatsCommandResponse ok(Object data) {
        return new NatsCommandResponse(true, data, null);
    }

    public static NatsCommandResponse error(String error) {
        return new NatsCommandResponse(false, null, error);
    }
}
