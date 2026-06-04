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
                reservation.getSchedule());
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Reservation reservation) {
        throw new UnsupportedOperationException();
    }
}
