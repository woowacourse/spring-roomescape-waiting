package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

public class FakeReservationDao implements ReservationRepository {

    List<Reservation> reservations = new ArrayList<>();
    Long index = 1L;

    @Override
    public Reservation save(final Reservation reservation) {
        Reservation newReservation = reservation.withId(index++);
        reservations.add(newReservation);
        return newReservation;
    }

    @Override
    public <S extends Reservation> Iterable<S> saveAll(final Iterable<S> entities) {
        return null;
    }

    @Override
    public boolean existsById(final Long aLong) {
        return false;
    }

    @Override
    public List<Reservation> findAll() {
        return reservations;
    }

    @Override
    public Iterable<Reservation> findAllById(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(final Reservation entity) {

    }

    @Override
    public void deleteAllById(final Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(final Iterable<? extends Reservation> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(
            final Long memberId,
            final Long themeId,
            final LocalDate fromDate,
            final LocalDate toDate
    ) {
        return List.of();
    }

    @Override
    public List<Reservation> findByDateBetween(final LocalDate from, final LocalDate to) {
        return List.of();
    }

    @Override
    public void deleteById(final Long id) {
        Reservation reservation = findById(id).orElseThrow();
        reservations.remove(reservation);
    }

    @Override
    public boolean existsByTimeId(final Long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTime().getId() == timeId);
    }

    @Override
    public boolean existsByThemeId(final Long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTheme().getId() == themeId);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId) {
        return reservations.stream()
                .anyMatch(reservation ->
                        reservation.getTheme().getId() == themeId
                                && reservation.getDate().isEqual(date)
                                && reservation.getTime().getId() == timeId);
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId() == id)
                .findFirst();
    }
}
