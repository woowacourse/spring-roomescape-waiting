package roomescape.fake;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationRepositoryInterface;
import roomescape.theme.domain.Theme;

public class FakeReservationRepository implements ReservationRepositoryInterface {

    private final Map<Long, Reservation> reservations = new HashMap<>();

    private long sequence = 0;

    @Override
    public Reservation save(final Reservation reservation) {
        sequence++;
        Reservation newReservation = new Reservation(
                sequence,
                reservation.getMember(),
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime()
        );
        reservations.put(sequence, newReservation);
        return newReservation;
    }

    @Override
    public List<Theme> findPopularThemesByReservationBetween(
            final LocalDate dateFrom,
            final LocalDate dateTo,
            final PageRequest pageRequest) {

        return reservations.values().stream()
                .filter(reservation ->
                        !reservation.getDate().isBefore(dateFrom) &&
                                !reservation.getDate().isAfter(dateTo)
                )
                .map(Reservation::getTheme)
                .distinct()
                .toList();
    }

    @Override
    public List<Reservation> findAll() {
        return reservations.values().stream().toList();
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return Optional.ofNullable(reservations.get(id));
    }

    @Override
    public void deleteById(final Long id) {
        reservations.remove(id);
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme) {
        return reservations.values().stream()
                .anyMatch(reservation ->
                        reservation.getDate().equals(date) &&
                                reservation.getTime().equals(time) &&
                                reservation.getTheme().equals(theme)
                );
    }

    @Override
    public List<Reservation> findByMember(final Member member) {
        return reservations.values().stream()
                .filter(reservation -> reservation.getMember().equals(member))
                .toList();
    }
}
