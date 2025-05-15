package roomescape.support.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();
    private Long index = 1L;

    public Reservation save(final Reservation reservation) {
        final Reservation newReservation = new Reservation(index++, reservation.getMember(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme());
        reservations.add(newReservation);
        return newReservation;
    }

    @Override
    public List<Reservation> findAll() {
        return reservations;
    }

    @Override
    public void deleteById(final long id) {
        final Reservation reservation = findById(id);
        reservations.remove(reservation);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getDate().equals(date) &&
                        reservation.getTime().getId() == timeId &&
                        reservation.getTheme().getId() == themeId);
    }

    @Override
    public boolean existsByTimeId(final long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTime().getId() == timeId);
    }

    @Override
    public boolean existsByThemeId(final long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTheme().getId() == themeId);
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(final LocalDate date, final long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTheme().getId() == themeId)
                .toList();
    }

    @Override
    public List<Reservation> findAllByMemberId(final long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getMember().getId() == id)
                .toList();
    }

    @Override
    public List<Reservation> findAllByCondition(final Long memberId, final Long themeId, final LocalDate from,
                                                final LocalDate to) {
        return reservations.stream()
                .filter(reservation -> memberId == null || reservation.isMemberHasSameId(memberId))
                .filter(reservation -> themeId == null || reservation.isThemeHasSameId(themeId))
                .filter(reservation -> reservation.isBetween(from, to))
                .toList();
    }

    public Reservation findById(final long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId() == id)
                .findFirst()
                .orElseThrow();
    }
}
