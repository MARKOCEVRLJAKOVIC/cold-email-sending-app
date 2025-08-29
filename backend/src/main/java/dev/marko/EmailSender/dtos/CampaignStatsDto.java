package dev.marko.EmailSender.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

public record CampaignStatsDto(
    int total,
    int sent,
    int failed,
    int pending,
    int replied
) {}
