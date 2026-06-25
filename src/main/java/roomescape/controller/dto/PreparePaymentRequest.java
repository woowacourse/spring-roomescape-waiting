package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;

public record PreparePaymentRequest(@NotNull Long amount) {}
