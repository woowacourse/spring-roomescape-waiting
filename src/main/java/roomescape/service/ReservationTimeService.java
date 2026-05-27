package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.ReservationTime;
import roomescape.dto.response.AvailableTimeResponse;
import roomescape.exception.ReservationTimeInUseException;
import roomescape.exception.ReservationTimeNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao) {
        this.reservationTimeDao = reservationTimeDao;
    }

    public List<ReservationTime> getReservationTimes() {
        return reservationTimeDao.findAllReservationTimes();
    }

    @Transactional
    public ReservationTime createReservationTime(LocalTime time) {
        Long id = reservationTimeDao.insertWithKeyHolder(time);
        return new ReservationTime(id, time);
    }

    @Transactional
    public void deleteReservationTime(Long id) {
        try {
            int deleteCount = reservationTimeDao.delete(id);
            validateDeleted(deleteCount);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationTimeInUseException();
        }
    }

    public List<AvailableTimeResponse> getAvailableTimes(LocalDate date, Long id) {
        Map<ReservationTime, Boolean> reservationTimeBooleanMap = reservationTimeDao.findAvailableTimes(date, id);
        return AvailableTimeResponse.fromAll(reservationTimeBooleanMap);
    }

    private void validateDeleted(int deleteCount) {
        if (deleteCount == 0) {
            throw new ReservationTimeNotFoundException();
        }
    }
}
