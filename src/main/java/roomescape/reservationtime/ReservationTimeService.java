package roomescape.reservationtime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservationtime.dto.AvailableTimeResponse;
import roomescape.reservationtime.exception.ReservationTimeInUseException;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao) {
        this.reservationTimeDao = reservationTimeDao;
    }

    @Transactional
    public ReservationTime createReservationTime(LocalTime time) {
        Long id = reservationTimeDao.insertWithKeyHolder(time);
        return new ReservationTime(id, time);
    }

    public List<ReservationTime> getReservationTimes() {
        return reservationTimeDao.findAllReservationTimes();
    }

    public List<AvailableTimeResponse> getAvailableTimes(LocalDate date, Long id) {
        Map<ReservationTime, Long> reservationTimeReservationIdMap = reservationTimeDao.findAvailableTimes(date, id);
        return AvailableTimeResponse.fromAll(reservationTimeReservationIdMap);
    }

    public List<AvailableTimeResponse> getAvailableTimes(LocalDate date, Long themeId, Long storeId) {
        return reservationTimeDao.findAvailableTimes(date, themeId, storeId).stream()
                .map(AvailableTimeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReservationTime(Long id) {
        try {
            reservationTimeDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationTimeInUseException();
        }
    }
}
