package roomescape.unit.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {
    private final List<Reservation> values = new ArrayList<>();
    private final AtomicLong increment = new AtomicLong(1);

    @Override
    public Reservation save(
            final Member member,
            final ReservationDateTime reservationDateTime,
            final Theme theme
    ) {
        Reservation reservation = new Reservation(
                increment.getAndIncrement(),
                member,
                reservationDateTime.getReservationDate(),
                reservationDateTime.getReservationTime(),
                theme
        );
        values.add(reservation);
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return values.stream().filter(reservation -> reservation.getId().equals(id)).findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public void deleteById(final Long id) {
        values.removeIf(reservation -> reservation.getId().equals(id));
    }

    @Override
    public boolean existSameDateTime(final ReservationDate reservationDate, final Long timeId) {
        return values.stream().anyMatch(reservation ->
                reservation.getReservationDate().equals(reservationDate) &&
                        reservation.getReservationTime().getId().equals(timeId)
        );
    }

    @Override
    public boolean existReservationByTimeId(final Long timeId) {
        return values.stream().anyMatch(reservation -> reservation.getReservationTime().getId().equals(timeId));
    }

    @Override
    public boolean existsByTheme_Id(final Long themeId) {
        return values.stream().anyMatch(reservation -> reservation.getTheme().getId().equals(themeId));
    }

    @Override
    public List<Reservation> findAllWithCondition(
            final Long memberId,
            final Long themeId,
            final LocalDate fromDate,
            final LocalDate toDate
    ) {
        return findAll();
    }
}
