package roomescape.reservation.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import roomescape.auth.dto.LoginMember;

public record ReservationCreateRequest(
        @FutureOrPresent @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId,
        @NotNull LoginMember loginMember
) {

    public static ReservationCreateRequest from(final ReservationRequest request, final LoginMember loginMember) {
        return new ReservationCreateRequest(request.date(), request.timeId(), request.themeId(), loginMember);
    }
}
