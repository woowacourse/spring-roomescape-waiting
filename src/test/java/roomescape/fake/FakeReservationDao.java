package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

public class FakeReservationDao implements ReservationRepository {

    List<Reservation> reservations = new ArrayList<>();
    Long index = 1L;

    @Override
    public Reservation save(final Reservation reservation) {
        Reservation newReservation = new Reservation(index++, reservation.getMember(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme());
        reservations.add(newReservation);
        return newReservation;
    }

    @Override
    public List<Reservation> findAll() {
        return reservations;
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId() == id)
                .findFirst();
    }

    @Override
    public List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(
            final long memberId,
            final long themeId,
            final LocalDate fromDate,
            final LocalDate toDate
    ) {
        return List.of();
    }

    @Override
    public List<Reservation> findAllByMemberIdOrderByDateDesc(final long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getMember().getId() == id)
                .sorted(Comparator.comparing(Reservation::getDate).reversed())
                .toList();
    }

    @Override
    public void deleteById(final Long id) {
        Reservation reservation = findById(id).orElseThrow();
        reservations.remove(reservation);
    }

    @Override
    public boolean existsById(final Long aLong) {
        return false;
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
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId) {
        return reservations.stream()
                .anyMatch(reservation ->
                        reservation.getTheme().getId() == themeId
                                && reservation.getDate().isEqual(date)
                                && reservation.getTime().getId() == timeId);
    }

    @Override
    public Iterable<Reservation> findAllById(final Iterable<Long> longs) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public <S extends Reservation> Iterable<S> saveAll(final Iterable<S> entities) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public long count() {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void delete(final Reservation entity) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAllById(final Iterable<? extends Long> longs) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAll(final Iterable<? extends Reservation> entities) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAll() {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public List<Reservation> findByDateBetween(final LocalDate from, final LocalDate to) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }
}
