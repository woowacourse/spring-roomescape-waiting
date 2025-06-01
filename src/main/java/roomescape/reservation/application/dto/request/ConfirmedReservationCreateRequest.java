package roomescape.reservation.application.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.reservation.presentation.dto.request.AdminReservationSlotCreateWebRequest;
import roomescape.reservation.presentation.dto.request.ConfirmedReservationCreateWebRequest;

public record ConfirmedReservationCreateRequest(LocalDate reservationDate, Long timeId, Long themeId, Long memberId,
                                                LocalDateTime reservationDateTime) {
    public static ConfirmedReservationCreateRequest of(
            final ConfirmedReservationCreateWebRequest request, final MemberInfo memberInfo) {
        return new ConfirmedReservationCreateRequest(request.date(),
                request.timeId(), request.themeId(), memberInfo.id(), LocalDateTime.now());
    }

    public static ConfirmedReservationCreateRequest of(final AdminReservationSlotCreateWebRequest request) {
        return new ConfirmedReservationCreateRequest(request.date(),
                request.timeId(), request.themeId(), request.memberId(), LocalDateTime.now());
    }
}
