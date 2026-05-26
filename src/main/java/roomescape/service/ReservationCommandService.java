package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidReferenceException;
import roomescape.exception.PastReservationException;
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

    private boolean isPast(LocalDate date, ReservationTime time) {
        return date.atTime(time.startAt()).isBefore(LocalDateTime.now(clock));
    }

    public Reservation create(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = findTimeReference(timeId);
        findThemeReference(themeId);
        if (isPast(date, time)) {
            throw new PastReservationException("지나간 시간에는 예약을 생성할 수 없습니다.");
        }
        if (reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new DuplicateException("해당 날짜와 시간에 이미 예약이 존재합니다.");
        }
        return reservationDao.save(name, date, timeId, themeId);
    }

    public void delete(long reservationId) {
        reservationDao.delete(reservationId);
    }

    public void cancel(long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId);
        if (isPast(reservation.reservationDate(), reservation.reservationTime())) {
            throw new PastReservationException("이미 시작된 예약은 취소할 수 없습니다.");
        }
        reservationDao.delete(reservationId);
    }

    public Reservation update(long reservationId, LocalDate newDate, long newTimeId) {
        ReservationTime newTime = findTimeReference(newTimeId);
        if (isPast(newDate, newTime)) {
            throw new PastReservationException("지나간 시간으로 예약을 변경할 수 없습니다.");
        }
        Reservation current = reservationDao.findById(reservationId);
        long themeId = current.reservationTheme().id();
        if (reservationDao.existsByDateAndTimeIdAndThemeIdExcluding(newDate, newTimeId, themeId, reservationId)) {
            throw new DuplicateException("변경하려는 시간에 이미 다른 예약이 존재합니다.");
        }
        return reservationDao.updateDateAndTime(reservationId, newDate, newTimeId);
    }
}
