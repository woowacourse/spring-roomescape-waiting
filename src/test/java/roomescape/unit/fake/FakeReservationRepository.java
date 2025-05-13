package roomescape.unit.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.dto.request.ReservationCondition;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);

    public FakeReservationRepository(Reservation... reservations) {
        Arrays.stream(reservations).forEach(reservation -> this.reservations.add(reservation));
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations);
    }

    @Override
    public Reservation save(Reservation reservation) {
        Reservation reservationWithId = Reservation.of(index.getAndIncrement(), reservation.getMember(),
                reservation.getDate(), reservation.getReservationTime(), reservation.getTheme());
        reservations.add(reservationWithId);
        return reservationWithId;
    }

    @Override
    public void deleteById(Long id) {
        reservations.removeIf(reservation -> reservation.getId().equals(id));
    }

    @Override
    public List<Reservation> findByTimeId(Long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getReservationTime().getId().equals(id))
                .toList();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Reservation> findByDateTimeAndTheme(LocalDate date, ReservationTime time, Theme theme) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getReservationTime().getId().equals(time.getId()))
                .filter(reservation -> reservation.getTheme().getId().equals(theme.getId()))
                .findFirst();
    }

    @Override
    public List<Reservation> findByCondition(ReservationCondition cond) {
        List<Reservation> filteredReservations = new ArrayList<>(reservations);
        if (cond.memberId().isPresent()) {
            filteredReservations = filteredReservations.stream()
                    .filter(reservation -> cond.memberId().get().equals(reservation.getMember().getId()))
                    .toList();
        }
        if (cond.themeId().isPresent()) {
            filteredReservations = filteredReservations.stream()
                    .filter(reservation -> cond.themeId().get().equals(reservation.getTheme().getId()))
                    .toList();
        }
        if (cond.dateFrom().isPresent() && cond.dateTo().isPresent()) {
            filteredReservations = filteredReservations.stream()
                    .filter(reservation -> reservation.getDate().isAfter(cond.dateFrom().get().minusDays(1)))
                    .filter(reservation -> reservation.getDate().isBefore(cond.dateTo().get().plusDays(1)))
                    .toList();
        }
        return filteredReservations;
    }

    @Override
    public List<Reservation> findByThemeId(Long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getTheme().getId().equals(themeId))
                .toList();
    }
}
