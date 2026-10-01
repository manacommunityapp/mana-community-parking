package com.manacommunity.parking.dto;

public record AnprWebhookResponse(
    boolean openBarrier,
    String matchType,
    String message,
    String plate,
    String assignedSlotNumber
) {}
