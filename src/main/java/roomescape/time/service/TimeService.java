package roomescape.time.service;

import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.ConflictException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.time.dao.TimeDao;
import roomescape.time.domain.Time;
import roomescape.time.dto.TimeRequest;
import roomescape.time.dto.TimeResponse;

@Service
public class TimeService {

    private final TimeDao timeDao;
    private final ReservationDao reservationDao;

    public TimeService(TimeDao timeDao, ReservationDao reservationDao) {
        this.timeDao = timeDao;
        this.reservationDao = reservationDao;
    }

    public TimeResponse addReservationTime(TimeRequest timeRequest) {
        validateDuplicateTime(timeRequest.startAt());
        Time reservationTime = new Time(timeRequest.startAt());
        Time savedReservationTime = timeDao.save(reservationTime);

        return TimeResponse.toResponse(savedReservationTime);
    }

    private void validateDuplicateTime(LocalTime startAt) {
        int duplicateTimeCount = timeDao.countByStartAt(startAt);
        if (duplicateTimeCount > 0) {
            throw new ConflictException("이미 존재하는 예약 시간입니다.");
        }
    }

    public List<TimeResponse> findReservationTimes() {
        List<Time> reservationTimes = timeDao.findAllReservationTimesInOrder();

        return reservationTimes.stream()
                .map(TimeResponse::toResponse)
                .toList();
    }

    public void removeReservationTime(long reservationTimeId) {
        validateReservationExistence(reservationTimeId);
        timeDao.deleteById(reservationTimeId);
    }

    private void validateReservationExistence(long timeId) {
        int reservationCount = reservationDao.countByTimeId(timeId);
        if (reservationCount > 0) {
            throw new ConflictException("삭제를 요청한 시간에 예약이 존재합니다.");
        }
    }

}
