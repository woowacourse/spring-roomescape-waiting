package roomescape.repository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> reservationTimes;
    private final AtomicLong reservationTimeId;
    private final List<Reservation> reservations = new ArrayList<>();

    public FakeReservationTimeRepository(final List<ReservationTime> reservationTimes) {
        this.reservationTimes = new ArrayList<>(reservationTimes);
        this.reservationTimeId = new AtomicLong(reservationTimes.size() + 1);
    }

    @Override
    public Optional<ReservationTime> save(ReservationTime reservationTime) {
        long count = reservationTimes.stream()
                .filter(rt -> rt.getStartAt().equals(reservationTime.getStartAt()))
                .count();
        if (count != 0) {
            throw new DuplicateKeyException("동일한 시간이 존재합니다.");
        }

        ReservationTime newReservationTime = new ReservationTime(reservationTimeId.getAndIncrement(), reservationTime.getStartAt());
        reservationTimes.add(newReservationTime);
        return findById(newReservationTime.getId());
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimes;
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        return reservationTimes.stream()
                .filter(reservationTime -> Objects.equals(reservationTime.getId(), id))
                .findFirst();
    }

    @Override
    public int deleteById(long id) {
        ReservationTime deleteReservation = reservationTimes.stream()
                .filter(reservationTime -> Objects.equals(reservationTime.getId(), id))
                .findFirst().orElse(new ReservationTime(null, LocalTime.now()));

        if (deleteReservation.getId() != null) {
            if (reservations.stream()
                    .filter(reservation -> reservation.getTime().equals(deleteReservation))
                    .count() != 0) {
                throw new DataIntegrityViolationException("연결된 예약 데이터로 인해 삭제할 수 없습니다.");
            }
            int affectedRows = (int) reservationTimes.stream()
                    .filter(reservationTime -> Objects.equals(reservationTime.getId(), id))
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
