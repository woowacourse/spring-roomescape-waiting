package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.dto.ReservationTimeAvailability;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.AvailableReservationTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.exception.code.ReservationErrorCode;
import roomescape.exception.code.ReservationTimeErrorCode;
import roomescape.exception.code.ThemeErrorCode;
import roomescape.exception.domain.ReservationException;
import roomescape.exception.domain.ReservationTimeException;
import roomescape.exception.domain.ThemeException;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;


    public ReservationTimeService(ReservationTimeDao reservationTimeDao, ThemeDao themeDao, ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    @Transactional
    public ReservationTimeResponse create(ReservationTimeRequest request) {
        ReservationTime reservationTime = request.toReservationTime();
        validateUniqueTime(reservationTime.getStartAt());
        ReservationTime newReservationTime = reservationTimeDao.save(reservationTime);
        return ReservationTimeResponse.from(newReservationTime);
    }

    private void validateUniqueTime(LocalTime startAt) {
        boolean exists = reservationTimeDao.existsByStartAt(startAt);
        if (exists) {
            throw new ReservationTimeException(ReservationTimeErrorCode.RESERVATION_TIME_ALREADY_EXISTS);
        }
    }

    public List<AvailableReservationTimeResponse> getReservationTimes(long themeId, LocalDate baseDate, LocalDate currentDate) {
        validateTheme(themeId);
        validateDate(baseDate, currentDate);
        List<ReservationTimeAvailability> timeAvailabilities = reservationTimeDao.findAvailabilitiesByThemeIdAndDate(themeId, baseDate);
        return timeAvailabilities.stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();
    }

    private void validateTheme(long themeId) {
        boolean exists = themeDao.existsById(themeId);
        if (!exists) {
            throw new ThemeException(ThemeErrorCode.THEME_NOT_FOUND);
        }
    }

    private void validateDate(LocalDate baseDate, LocalDate currentDate) {
        boolean exists = baseDate.isBefore(currentDate);
        if (exists) {
            throw new ReservationException(ReservationErrorCode.PAST_DATE_NOT_ALLOWED);
        }
    }

    @Transactional
    public void delete(long reservationTimeId) {
        validateReservationNotExistsBy(reservationTimeId);
        int affectedRows = reservationTimeDao.delete(reservationTimeId);

        if (affectedRows == 0) {
            throw new ReservationTimeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND);
        }
    }

    private void validateReservationNotExistsBy(long reservationTimeId) {
        if (reservationDao.existsByReservationTime(reservationTimeId)) {
            throw new ReservationTimeException(ReservationTimeErrorCode.RESERVATION_TIME_HAS_RESERVATION);
        }
    }
}
