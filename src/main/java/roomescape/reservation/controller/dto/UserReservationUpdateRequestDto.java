package roomescape.reservation.controller.dto;

import jakarta.validation.constraints.NotNull;

public record UserReservationUpdateRequestDto(@NotNull Long timeId) {}
