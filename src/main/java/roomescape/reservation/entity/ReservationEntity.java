package roomescape.reservation.entity;

import java.time.LocalDate;
import roomescape.member.entity.MemberEntity;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservationtime.entity.ReservationTimeEntity;
import roomescape.theme.entity.ThemeEntity;

public record ReservationEntity(
        Long id,
        LocalDate date,
        MemberEntity memberEntity,
        ReservationTimeEntity timeEntity,
        ThemeEntity themeEntity,
        ReservationStatus reservationStatus
) {
    public ReservationEntity(final Long id, final String date, final MemberEntity memberEntity,
                             final ReservationTimeEntity timeEntity, final ThemeEntity themeEntity,  ReservationStatus reservationStatus) {
        this(id, LocalDate.parse(date), memberEntity, timeEntity, themeEntity, reservationStatus);
    }

    public Reservation toReservation() {
        return new Reservation(memberEntity.toMember(), date, timeEntity.toReservationTime(), themeEntity.toTheme(), reservationStatus);
    }
}
