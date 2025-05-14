package roomescape.repository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> reservationTimes;
    private final AtomicLong reservationTimeId = new AtomicLong(1);
    private final List<Reservation> reservations;

    public FakeReservationTimeRepository(final List<ReservationTime> reservationTimes) {
        this.reservationTimes = reservationTimes;
        reservations = new ArrayList<>();
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        long count = reservationTimes.stream()
                .filter(rt -> rt.startAt().equals(reservationTime.startAt()))
                .count();
        if(count != 0){
            throw new IllegalStateException("Reservation time already exists");
        }

        ReservationTime newReservationTime = new ReservationTime(reservationTimeId.getAndIncrement(), reservationTime.startAt());
        reservationTimes.add(newReservationTime);
        return findById(newReservationTime.id()).get();
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimes;
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        return reservationTimes.stream()
                .filter(reservationTime -> Objects.equals(reservationTime.id(), id))
                .findFirst();
    }

    @Override
    public int deleteById(long id) {
        ReservationTime deleteReservation = reservationTimes.stream()
                .filter(reservationTime -> Objects.equals(reservationTime.id(), id))
                .findFirst().orElse(new ReservationTime(null, LocalTime.now()));

        if (deleteReservation.id() != null) {
            if (reservations.stream()
                    .filter(reservation -> reservation.getTime().equals(deleteReservation))
                    .count() != 0) {
                throw new IllegalStateException();
            }
            int affectedRows = (int) reservationTimes.stream()
                    .filter(reservationTime -> Objects.equals(reservationTime.id(), id))
                    .count();
            reservationTimes.remove(deleteReservation);
            return affectedRows;
        }

        return 0;
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }
}
