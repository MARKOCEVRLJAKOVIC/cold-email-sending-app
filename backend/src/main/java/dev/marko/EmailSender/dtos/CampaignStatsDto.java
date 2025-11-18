package dev.marko.EmailSender.dtos;

public record CampaignStatsDto(
    int total,
    int sent,
    int failed,
    int pending,
    int replied
) {}
