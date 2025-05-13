package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.Reservation;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations;
    private final AtomicLong reservationId;

    public FakeReservationRepository(List<Reservation> reservations) {
        this.reservations = new ArrayList<>(reservations);
        this.reservationId = new AtomicLong(reservations.size() + 1);
    }

    @Override
    public long save(Reservation reservation) {
        List<Reservation> existingReservations = findByDateTimeTheme(reservation.getDate(), reservation.getTime().getStartAt(), reservation.getTheme().getId());
        if (!existingReservations.isEmpty()) {
            throw new DuplicateKeyException("동일한 예약이 존재합니다.");
        }
        long id = reservationId.getAndIncrement();
        Reservation newReservation = new Reservation(id, reservation.getName(), reservation.getDate(), reservation.getTime(), reservation.getTheme());
        reservations.add(newReservation);
        return id;
    }

    @Override
    public List<Reservation> findAll() {
        return reservations;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getId(), id))
                .findFirst();
    }

    @Override
    public List<Reservation> findByDateTimeTheme(LocalDate date, LocalTime time, long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date) && reservation.getTime().getStartAt().equals(time) && reservation.getTheme().getId().equals(themeId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByDateAndTheme(LocalDate date, long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date) && reservation.getTheme().getId().equals(themeId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByThemeMemberDateRange(Long themeId, Long memberId, LocalDate from, LocalDate to) {
        return reservations.stream()
                .filter(r -> themeId == null || r.getTheme().getId().equals(themeId))
                .filter(r -> memberId == null || r.getName().getId().equals(memberId))
                .filter(r -> from == null || !r.getDate().isBefore(from))
                .filter(r -> to == null || !r.getDate().isAfter(to))
                .collect(Collectors.toList());
    }

    @Override
    public int deleteById(long id) {
        Reservation deleteReservation = reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getId(), id))
                .findFirst().orElse(null);

        if (deleteReservation != null) {
            int affectedRows = (int) reservations.stream()
                    .filter(reservation -> Objects.equals(reservation.getId(), deleteReservation.getId()))
                    .count();

            reservations.remove(deleteReservation);
            return affectedRows;
        }
        return 0;
    }
}
