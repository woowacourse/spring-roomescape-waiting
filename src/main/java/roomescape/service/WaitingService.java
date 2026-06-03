package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingLine;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.exception.NotOwnerException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.WaitingWithNumber;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository,
                          TimeSlotRepository timeSlotRepository, ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public WaitingWithNumber saveWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        Waiting waiting = createWaiting(name, date, timeId, themeId);
        validateAlreadyReserved(name, date, timeId, themeId);
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

    private Waiting createWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        TimeSlot timeSlot = findTimeSlot(timeId);
        Theme theme = findTheme(themeId);
        return new Waiting(name, date, timeSlot, theme, LocalDateTime.now());
    }

    private void validateDuplicatedWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        if (waitingRepository.exists(name, date, timeId, themeId)) {
            throw new DuplicateException("해당 날짜의 시간과 테마는 이미 예약 대기되어 있습니다.");
        }
    }

    private void validateAlreadyReserved(String name, LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByNameAndDateAndTimeAndTheme(name, date, timeId, themeId)) {
            throw new DuplicateException("이미 예약된 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
        }
    }

    private void validateWaitingOwner(Waiting waiting, String requestName) {
        if (!waiting.isOwner(requestName)) {
            throw new NotOwnerException();
        }
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

    private TimeSlot findTimeSlot(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 시간대를 찾을 수 없습니다."));
    }

    private Theme findTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 테마를 찾을 수 없습니다."));
    }
}
