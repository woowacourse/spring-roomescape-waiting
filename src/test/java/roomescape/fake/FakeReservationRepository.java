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
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> reservations = new HashMap<>();

    private long sequence = 0;

    @Override
    public <S extends Reservation> S save(final S entity) {
        sequence++;
        Reservation reservation = new Reservation(
                sequence,
                entity.getMember(),
                entity.getTheme(),
                entity.getDate(),
                entity.getTime());
        reservations.put(sequence, reservation);
        return (S) reservation;
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
    public List<Reservation> findByThemeAndMemberAndDateBetween(
            final Theme theme,
            final Member member,
            final LocalDate dateFrom,
            final LocalDate dateTo) {
        return reservations.values().stream()
                .filter(reservation ->
                        reservation.getTheme().equals(theme) &&
                                reservation.getMember().equals(member) &&
                                !reservation.getDate().isBefore(dateFrom) &&
                                !reservation.getDate().isAfter(dateTo)
                )
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

    @Override
    public <S extends Reservation> Iterable<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public Iterable<Reservation> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(Reservation entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends Reservation> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
