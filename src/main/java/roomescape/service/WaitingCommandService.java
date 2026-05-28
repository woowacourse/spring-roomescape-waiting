package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Waiting;
import roomescape.exception.*;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WaitingCommandService {
    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    private Waiting findWaitingReference(long waitingId) {
        try {
            return waitingDao.findById(waitingId);
        } catch (ResourceNotFoundException e) {
            throw new InvalidReferenceException("존재하지 않는 예약 대기입니다.");
        }
    }

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

    public Waiting create(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = findTimeReference(timeId);
        findThemeReference(themeId);

        Reservation reservation = reservationDao.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 날짜와 시간에 예약이 존재하지 않습니다."));

        time.validateNotPast(date, LocalDateTime.now(clock));

        if (reservation.isOwnedBy(name)) {
            throw new DuplicateException("내가 예약한 시간에 예약대기를 생성할 수 없습니다.");
        }

        if (waitingDao.findAllByDateAndTimeIdAndThemeId(date, timeId, themeId).stream()
                .anyMatch(waiting -> name.equals(waiting.name()))) {
            throw new DuplicateException("같은 날짜/시간/테마에 여러 개의 예약 대기를 생성할 수 없습니다.");
        }

        return waitingDao.save(name, date, timeId, themeId, LocalDateTime.now(clock));
    }

    public void cancel(long waitingId, String name) {
        Waiting waiting = findWaitingReference(waitingId);

        waiting.validateCancelable(LocalDateTime.now(clock));
        waiting.validateOwnedBy(name);

        waitingDao.delete(waitingId);
    }
}
