package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidReferenceException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.ReservationDao;
import roomescape.repository.ReservationTimeDao;
import roomescape.repository.ThemeDao;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    private ReservationTime findTimeReference(long timeId) {
        try {
            return reservationTimeDao.findById(timeId);
        } catch (ResourceNotFoundException e) {
            throw new InvalidReferenceException("존재하지 않는 예약 시간입니다.");
        }
    }

    private void findThemeReference(long themeId) {
        try {
            themeDao.findById(themeId);
        } catch (ResourceNotFoundException e) {
            throw new InvalidReferenceException("존재하지 않는 테마입니다.");
        }
    }

    public Reservation create(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = findTimeReference(timeId);
        findThemeReference(themeId);
        time.validateNotPast(date, LocalDateTime.now(clock));
        if (reservationDao.findByDateAndTimeIdAndThemeId(date, timeId, themeId).isPresent()) {
            throw new DuplicateException("해당 날짜와 시간에 이미 예약이 존재합니다.");
        }
        return reservationDao.save(name, date, timeId, themeId);
    }

    public void delete(long reservationId) {
        reservationDao.delete(reservationId);
    }

    public void cancel(long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId);
        reservation.validateCancelable(LocalDateTime.now(clock));
        reservationDao.delete(reservationId);
    }

    public Reservation update(long reservationId, LocalDate newDate, long newTimeId) {
        ReservationTime newTime = findTimeReference(newTimeId);
        newTime.validateNotPast(newDate, LocalDateTime.now(clock));
        Reservation current = reservationDao.findById(reservationId);
        long themeId = current.reservationTheme().id();
        boolean isDuplicate = reservationDao.findByDateAndTimeIdAndThemeId(newDate, newTimeId, themeId)
                .filter(existing -> existing.id() != reservationId)
                .isPresent();
        if (isDuplicate) {
            throw new DuplicateException("변경하려는 시간에 이미 다른 예약이 존재합니다.");
        }
        return reservationDao.updateDateAndTime(reservationId, newDate, newTimeId);
    }
}
