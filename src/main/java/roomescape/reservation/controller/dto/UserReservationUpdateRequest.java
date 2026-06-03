package roomescape.reservation.controller.dto;

import jakarta.validation.constraints.NotNull;

public record UserReservationUpdateRequest(@NotNull Long timeId) {}
