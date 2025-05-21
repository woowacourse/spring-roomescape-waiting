package roomescape.reservation.service.converter;

import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.service.dto.CreateReservationServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public class ReservationWaitConverter {

    public static ReservationWait toDomain(final CreateReservationServiceRequest request,
                                           final Member member,
                                           final ReservationTime time,
                                           final Theme theme) {
        return ReservationWait.withoutId(
                member,
                ReservationDate.from(request.date()),
                time,
                theme
        );
    }
}
