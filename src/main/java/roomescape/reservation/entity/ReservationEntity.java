package roomescape.reservation.entity;

import java.time.LocalDate;
import roomescape.member.entity.MemberEntity;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.entity.ReservationTimeEntity;
import roomescape.theme.entity.ThemeEntity;

public record ReservationEntity(
        Long id,
        LocalDate date,
        MemberEntity memberEntity,
        ReservationTimeEntity timeEntity,
        ThemeEntity themeEntity
) {
    public ReservationEntity(final Long id, final String date, final MemberEntity memberEntity,
                             final ReservationTimeEntity timeEntity, final ThemeEntity themeEntity) {
        this(id, LocalDate.parse(date), memberEntity, timeEntity, themeEntity);
    }

    public Reservation toReservation() {
        return Reservation.of(id, date, memberEntity.toMember(), timeEntity.toReservationTime(), themeEntity.toTheme());
    }
}
