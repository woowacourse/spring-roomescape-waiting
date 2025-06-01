package roomescape.support.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.common.exception.RoomescapeException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

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
        final Reservation newReservation = new Reservation(index++, reservation.date(), reservation.time(), reservation.theme(), reservation.member());
        reservations.add(newReservation);
        return newReservation;
    }

    @Override
    public void deleteById(final long id) {
        final Reservation reservation = findById(id)
                .orElseThrow(() -> new RoomescapeException("예약이 존재하지 않습니다."));;
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
    public List<Reservation> findAllByMemberId(final long memberId) {
        return reservations.stream()
                .filter(reservation -> reservation.member().id() == memberId)
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

    @Override
    public Optional<Reservation> findById(final long id) {
        return reservations.stream()
                .filter(reservation -> reservation.id() == id)
                .findFirst();
    }

    @Override
    public Optional<Reservation> findByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.date().equals(date) && reservation.time().id() == timeId && reservation.theme().id() == themeId)
                .findFirst();
    }
}
