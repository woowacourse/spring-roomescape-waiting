package roomescape.reservation.service.converter;

import roomescape.member.domain.Member;
import roomescape.member.service.MemberConverter;
import roomescape.reservation.controller.dto.ReservationWebResponse;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.service.dto.CreateReservationServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.converter.ThemeConverter;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.converter.ReservationTimeConverter;

public class ReservationWaitConverter {

    public static ReservationWait toDomain(
            final CreateReservationServiceRequest request,
            final Member member,
            final ReservationTime time,
            final Theme theme
    ) {
        return ReservationWait.withoutId(
                member,
                ReservationDate.from(request.date()),
                time,
                theme
        );
    }

    public static ReservationWebResponse toDto(final ReservationWait reservationWait) {
        return new ReservationWebResponse(
                reservationWait.getId(),
                MemberConverter.toDto(reservationWait.getMember()),
                reservationWait.getDate().getValue(),
                ReservationTimeConverter.toDto(reservationWait.getTime()),
                ThemeConverter.toDto(reservationWait.getTheme()));
    }
}
