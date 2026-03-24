package com.scania.warranty.dto;

public record AttachmentDto(
    Long workTicketId,
    String dealerId,
    String claimNo,
    String failNo,
    boolean attachmentsAdded
) {}