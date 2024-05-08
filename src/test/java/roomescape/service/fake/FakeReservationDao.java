package roomescape.service.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.repository.ReservationDao;

public class FakeReservationDao implements ReservationDao {

    private final List<Reservation> reservations = new ArrayList<>();

    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public List<Reservation> getAllReservations() {
        return reservations;
    }

    @Override
    public Reservation addReservation(Reservation reservation) {
        Reservation newReservation = new Reservation(index.getAndIncrement(),
                reservation.getDate(), reservation.getTime(), reservation.getTheme(), reservation.getMember());
        reservations.add(newReservation);
        return newReservation;
    }

    @Override
    public List<Reservation> searchReservation(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        System.out.println("reservations.size() = " + reservations.size());
        return reservations.stream()
                .filter(reservation -> reservation.getTheme().getId() == themeId
                        && reservation.getMember().getId() == memberId
                        && dateTo.isAfter(reservation.getDate())
                        && dateFrom.isBefore(reservation.getDate()))
                .toList();
    }

    @Override
    public void deleteReservation(long id) {
        Reservation foundReservation = reservations.stream()
                .filter(reservation -> reservation.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("아이디가 존재하지 않습니다."));
        reservations.remove(foundReservation);
    }

    @Override
    public long countReservationById(long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId() == id)
                .count();
    }

    @Override
    public long countReservationByTimeId(long timeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getTime().getId() == timeId)
                .count();
    }

    @Override
    public long countReservationByDateAndTimeId(LocalDate date, long timeId, long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().isEqual(date)
                        && reservation.getTime().getId() == timeId
                        && reservation.getTheme().getId() == themeId)
                .count();
    }

    @Override
    public List<ReservationTime> findReservationTimeByDateAndTheme(LocalDate date, long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date)
                        && reservation.getTheme().getId() == themeId)
                .map(reservation -> new ReservationTime(reservation.getTime().getId(),
                        reservation.getTime().getStartAt()))
                .toList();
    }

    public void clear() {
        index.set(1L);
        reservations.clear();
    }

    public int size() {
        System.out.println("reservations = " + reservations);
        return reservations.size();
    }
}
