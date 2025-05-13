package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.theme.Theme;

public interface ReservationRepository {

    Reservation save(
            final Member member,
            final ReservationDateTime reservationDateTime,
            final Theme theme
    );

    Optional<Reservation> findById(final Long id);

    List<Reservation> findAll();

    void deleteById(final Long id);

    boolean existSameDateTime(
            final ReservationDate reservationDate,
            final Long timeId
    );

    boolean existReservationByTimeId(final Long timeId);

    boolean existReservationByThemeId(final Long themeId);

    List<Reservation> findAllWithCondition(
            final Long memberId,
            final Long themeId,
            final LocalDate fromDate,
            final LocalDate toDate
    );
}
