package com.scoring.pmescoring.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Metrics summary of user accounts, status, and role distributions")
public record UserMetricsResponse(

        @Schema(description = "Total number of registered user accounts (including ACTIVE, INACTIVE, and DELETED)", example = "3")
        long totalUsersCount,

        @Schema(description = "Number of active user accounts with enabled access", example = "3")
        long activeUsersCount,

        @Schema(description = "Total number of users with administrative access role", example = "2")
        long adminUsersCount,

        @Schema(description = "Total number of operational analyst users", example = "1")
        long analystUsersCount
) {
}