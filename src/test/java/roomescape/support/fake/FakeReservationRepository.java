package roomescape.support.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.reservation.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();
    private Long index = 1L;

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.date().equals(date) &&
                        reservation.time().id() == timeId &&
                        reservation.theme().id() == themeId);
    }

    @Override
    public boolean existsByTimeId(final long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.time().id() == timeId);
    }

    @Override
    public boolean existsByThemeId(final long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.theme().id() == themeId);
    }

    @Override
    public Reservation save(final Reservation reservation) {
        final Reservation newReservation = new Reservation(index++, reservation.member(), reservation.date(),
                reservation.time(), reservation.theme());
        reservations.add(newReservation);
        return newReservation;
    }

    @Override
    public void deleteById(final long id) {
        final Reservation reservation = findById(id);
        reservations.remove(reservation);
    }

    @Override
    public List<Reservation> findAll() {
        return reservations;
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(final LocalDate date, final long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.date().equals(date))
                .filter(reservation -> reservation.theme().id() == themeId)
                .toList();
    }

    @Override
    public List<Reservation> findAllByMemberId(final long id) {
        return reservations.stream()
                .filter(reservation -> reservation.member().id() == id)
                .toList();
    }

    @Override
    public List<Reservation> findAllByCondition(final Long memberId, final Long themeId, final LocalDate from,
                                                final LocalDate to) {
        return reservations.stream()
                .filter(reservation -> memberId == null || reservation.isMemberSameId(memberId))
                .filter(reservation -> themeId == null || reservation.isThemeSameId(themeId))
                .filter(reservation -> reservation.isDateBetween(from, to))
                .toList();
    }

    public Reservation findById(final long id) {
        return reservations.stream()
                .filter(reservation -> reservation.id() == id)
                .findFirst()
                .orElseThrow();
    }
}
