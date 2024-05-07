package roomescape.service.fakedao;

import roomescape.model.ReservationTime;
import roomescape.repository.dao.ReservationTimeDao;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeReservationTimeDao implements ReservationTimeDao {

    private final AtomicLong index = new AtomicLong(1);
    private final List<ReservationTime> reservationTimes = new ArrayList<>();

    public FakeReservationTimeDao(List<ReservationTime> reservationTimes) {
        reservationTimes.forEach(this::save);
    }

    @Override
    public long save(ReservationTime reservationTime) {
        long key = index.getAndIncrement();
        reservationTimes.add(new ReservationTime(key, reservationTime.getStartAt()));
        return key;
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimes;
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        return reservationTimes.stream()
                .filter(reservationTime -> reservationTime.getId() == id)
                .findFirst();
    }

    @Override
    public void deleteById(long id) {
        ReservationTime findReservationTime = reservationTimes.stream()
                .filter(reservationTime -> reservationTime.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약 시간입니다."));
        reservationTimes.remove(findReservationTime);
    }

    @Override
    public Boolean isExistById(long id) {
        return reservationTimes.stream().anyMatch(reservationTime -> reservationTime.getId() == id);
    }

    @Override
    public Boolean isExistByStartAt(LocalTime startAt) {
        return reservationTimes.stream().anyMatch(reservationTime -> reservationTime.getStartAt().equals(startAt));
    }
}
