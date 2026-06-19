package roomescape.service.fake;

import roomescape.domain.Reservation;
import roomescape.domain.Schedule;
import roomescape.repository.ReservationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Reservation save(Reservation reservation) {
        Reservation saved = new Reservation(
                sequence.incrementAndGet(),
                reservation.getName(),
                reservation.getSchedule(),
                reservation.getStatus());
        reservations.add(saved);
        return saved;
    }

    @Override
    public Optional<Reservation> findBySchedule(Schedule schedule) {
        return reservations.stream()
                .filter(r -> r.getSchedule().equals(schedule))
                .findFirst();
    }

    @Override
    public Optional<String> findReserverNameByScheduleForUpdate(Schedule schedule) {
        return findBySchedule(schedule)
                .map(Reservation::getName);
    }

    public List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    @Override
    public List<Reservation> findAll(int page, int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Reservation> findById(long id) {
        return reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Reservation> findUserReservations(String name, int page, int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsByTimeId(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(Reservation reservation) {
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getId().equals(reservation.getId())) {
                reservations.set(i, reservation);
                return;
            }
        }
    }

    @Override
    public void confirm(long id) {
        findById(id).ifPresent(reservation -> update(reservation.confirm()));
    }

    @Override
    public boolean delete(Reservation reservation) {
        return reservations.removeIf(r -> r.getId().equals(reservation.getId()));
    }

    @Override
    public boolean deletePendingById(long id) {
        return reservations.removeIf(r -> r.getId().equals(id) && r.getStatus().name().equals("PENDING"));
    }
}
