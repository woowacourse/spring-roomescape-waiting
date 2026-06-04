package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.command.ReservationTimeCommand;
import roomescape.dto.response.CreateReservationTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final ReservationDao reservationDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao, ThemeDao themeDao,
                                  ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.reservationDao = reservationDao;
    }

    @Transactional
    public CreateReservationTimeResponse addReservationTime(ReservationTimeCommand command) {
        ReservationTime reservationTime = ReservationTime.createWithoutId(command.startAt());
        ReservationTime newReservationTime = reservationTimeDao.insert(reservationTime);
        return CreateReservationTimeResponse.from(newReservationTime);
    }

    public List<ReservationTimeResponse> getReservationTimes(Long themeId, LocalDate date) {
        validateTheme(themeId);

        List<Reservation> reservations = reservationDao.selectByThemeIdAndDate(themeId, date);
        List<ReservationTime> reservationTimes = reservationTimeDao.selectAll();

        return reservationTimes.stream()
                .map(time -> ReservationTimeResponse.from(time, time.isNotReserved(reservations)))
                .toList();
    }

    @Transactional
    public void deleteReservationTime(long reservationTimeId) {
        Optional<ReservationTime> reservationTime = reservationTimeDao.selectById(reservationTimeId);
        if (reservationTime.isEmpty()) {
            throw new RoomEscapeException(ReservationTimeErrorCode.NOT_FOUND);
        }

        validateTimeIncludeReservation(reservationTimeId);
        reservationTimeDao.delete(reservationTimeId);
    }

    private void validateTheme(Long themeId) {
        boolean exists = themeDao.existsById(themeId);
        if (!exists) {
            throw new RoomEscapeException(ThemeErrorCode.NOT_FOUND);
        }
    }

    private void validateTimeIncludeReservation(long reservationTimeId) {
        boolean existsByTimeId = reservationDao.existsByTimeId(reservationTimeId);
        if (existsByTimeId) {
            throw new RoomEscapeException(ThemeErrorCode.THEME_CANNOT_DELETE);
        }
    }
}
