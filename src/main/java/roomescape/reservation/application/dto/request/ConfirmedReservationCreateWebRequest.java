package roomescape.reservation.application.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.reservation.presentation.dto.request.AdminReservationSlotCreateWebRequest;

public record ConfirmedReservationCreateWebRequest(LocalDate date, Long timeId, Long themeId, Long memberId,
                                                   LocalDateTime now) {
    public static ConfirmedReservationCreateWebRequest of(
            final roomescape.reservation.presentation.dto.request.ConfirmedReservationCreateWebRequest request, final MemberInfo memberInfo) {
        return new ConfirmedReservationCreateWebRequest(request.date(),
                request.timeId(), request.themeId(), memberInfo.id(), LocalDateTime.now());
    }

    public static ConfirmedReservationCreateWebRequest of(final AdminReservationSlotCreateWebRequest request) {
        return new ConfirmedReservationCreateWebRequest(request.date(),
                request.timeId(), request.themeId(), request.memberId(), LocalDateTime.now());
    }
}
