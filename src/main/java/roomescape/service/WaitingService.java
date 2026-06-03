package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingLine;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.exception.NotOwnerException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;
import roomescape.domain.WaitingWithNumber;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public WaitingWithNumber saveWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        Reservation reservation = findReservation(date, timeId, themeId);
        Waiting waiting = Waiting.from(reservation, name, LocalDateTime.now());
        validateDuplicatedWaiting(name, date, timeId, themeId);
        Waiting saveWaiting = waitingRepository.save(waiting);
        return createWaitingWithNumber(saveWaiting);
    }

    @Transactional
    public void removeWaiting(Long id, String requestName) {
        Waiting waiting = findWaiting(id);
        validateWaitingOwner(waiting, requestName);
        waiting.validateCancelable(LocalDateTime.now());
        waitingRepository.deleteById(id);
    }

    private void validateDuplicatedWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        if (waitingRepository.exists(name, date, timeId, themeId)) {
            throw new DuplicateException("해당 날짜의 시간과 테마는 이미 예약 대기되어 있습니다.");
        }
    }

    private void validateWaitingOwner(Waiting waiting, String requestName) {
        if (!waiting.isOwner(requestName)) {
            throw new NotOwnerException();
        }
    }

    private Reservation findReservation(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new NotFoundException("예약되지 않은 슬롯입니다."));
    }

    private Waiting findWaiting(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당하는 예약 대기 정보를 찾을 수 없습니다."));
    }

    private WaitingWithNumber createWaitingWithNumber(Waiting waiting) {
        WaitingLine waitingLine = new WaitingLine(waitingRepository.findByDateAndTimeAndTheme(
                waiting.getDate(),
                waiting.getTimeSlot().getId(),
                waiting.getTheme().getId()
        ));
        return new WaitingWithNumber(waiting, waitingLine.findWaitingNumber(waiting));
    }
}
