package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ReservationTimeDao;
import roomescape.reservation.service.dto.AvailableTimeInfo;
import roomescape.reservation.service.dto.ReservationTimeCreateCommand;
import roomescape.reservation.service.dto.ReservationTimeInfo;

@Service
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;
    private final ReservationDao reservationDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao, ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.reservationDao = reservationDao;
    }

    public ReservationTimeInfo createReservationTime(final ReservationTimeCreateCommand command) {
        if (reservationTimeDao.isExistsByTime(command.startAt())) {
            throw new IllegalArgumentException("이미 존재하는 시간입니다.");
        }
        final ReservationTime reservationTime = command.convertToReservationTime();
        final ReservationTime savedReservationTime = reservationTimeDao.save(reservationTime);
        return new ReservationTimeInfo(savedReservationTime);
    }

    public List<ReservationTimeInfo> getReservationTimes() {
        return reservationTimeDao.findAll().stream()
                .map(ReservationTimeInfo::new)
                .toList();
    }

    public void deleteReservationTimeById(final long id) {
        if (reservationDao.isExistsByTimeId(id)) {
            throw new IllegalArgumentException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
        reservationTimeDao.deleteById(id);
    }

    public List<AvailableTimeInfo> findAvailableTimes(final LocalDate date, final long themeId) {
        final List<ReservationTime> reservationTimes = reservationTimeDao.findAll();
        final List<Reservation> reservations = reservationDao.findAllByDateAndThemeId(date, themeId);
        return reservationTimes.stream()
                .map(time ->
                        new AvailableTimeInfo(time.getId(), time.getStartAt(), isAlreadyBooked(time, reservations)))
                .toList();
    }

    private boolean isAlreadyBooked(final ReservationTime reservationTime, final List<Reservation> reservations) {
        return reservations.stream().anyMatch(reservation -> reservation.isSameTime(reservationTime));
    }
}
