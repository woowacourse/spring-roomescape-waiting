package roomescape.service.fake;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import roomescape.model.ReservationTime;
import roomescape.repository.ReservationTimeDao;

public class FakeReservationTimeDao implements ReservationTimeDao {

    private final List<ReservationTime> reservationTimes = new ArrayList<>();

    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimes;
    }

    @Override
    public List<ReservationTime> findAllReservedTimes(LocalDate date, long themeId) {
        return List.of(reservationTimes.get(0));
    }

    @Override
    public ReservationTime findReservationById(long id) {
        return reservationTimes.stream()
                .filter(reservationTime -> reservationTime.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("해당하는 아이디가 없습니다."));
    }

    @Override
    public ReservationTime addReservationTime(ReservationTime reservationTime) {
        ReservationTime newReservationTime = new ReservationTime(index.getAndIncrement(), reservationTime.getStartAt());
        reservationTimes.add(newReservationTime);
        return newReservationTime;
    }

    @Override
    public void deleteReservationTime(long id) {
        ReservationTime findReservationTime = reservationTimes.stream()
                .filter(reservationTime -> reservationTime.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("해당하는 아이디가 없습니다."));

        reservationTimes.remove(findReservationTime);
    }

    @Override
    public long countReservationTimeById(long id) {
        return reservationTimes.stream()
                .filter(reservationTime -> reservationTime.getId() == id)
                .count();
    }

    @Override
    public long countReservationTimeByStartAt(LocalTime startAt) {
        return reservationTimes.stream()
                .filter(reservationTime -> reservationTime.getStartAt() == startAt)
                .count();
    }

    public void add(ReservationTime reservationTime) {
        reservationTimes.add(reservationTime);
    }

    public void clear() {
        index.set(1L);
        reservationTimes.clear();
    }
}
