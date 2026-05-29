package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidReferenceException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.ReservationDao;
import roomescape.repository.ReservationTimeDao;
import roomescape.repository.ThemeDao;
import roomescape.repository.WaitingDao;

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

    public Waiting create(String name, LocalDate date, long timeId, long themeId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Slot slot = new Slot(date, findTimeReference(timeId), findThemeReference(themeId));
        Reservation reservation = findReservationBySlot(slot);

        slot.validateNotPast(now);
        validateNotOwnReservation(reservation, name);
        validateNoDuplicateWaiting(slot, name);

        return waitingDao.save(new Waiting(null, name, slot, now));
    }

    public void cancel(long waitingId, String name) {
        Waiting waiting = findWaitingReference(waitingId);

        waiting.validateOwnedBy(name);
        waiting.validateNotStarted(LocalDateTime.now(clock));

        waitingDao.deleteById(waitingId);
    }

    private Reservation findReservationBySlot(Slot slot) {
        return reservationDao.findBySlot(slot)
                .orElseThrow(() -> new ResourceNotFoundException("해당 날짜와 시간에 예약이 존재하지 않습니다."));
    }

    private void validateNotOwnReservation(Reservation reservation, String name) {
        if (reservation.isOwnedBy(name)) {
            throw new DuplicateException("내가 예약한 시간에 예약대기를 생성할 수 없습니다.");
        }
    }

    private void validateNoDuplicateWaiting(Slot slot, String name) {
        if (waitingDao.existsBySlotAndName(slot, name)) {
            throw new DuplicateException("같은 날짜/시간/테마에 여러 개의 예약 대기를 생성할 수 없습니다.");
        }
    }

    private Waiting findWaitingReference(long waitingId) {
        return waitingDao.findById(waitingId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 예약 대기입니다."));
    }

    private ReservationTime findTimeReference(long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 예약 시간입니다."));
    }

    private Theme findThemeReference(long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 테마입니다."));
    }
}
