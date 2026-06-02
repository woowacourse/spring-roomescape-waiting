package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateWaitingException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.exception.WaitingNotFoundException;
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
        validateDuplicatedWaiting(name, date, timeId, themeId);
        Waiting waiting = createWaiting(name, date, timeId, themeId);
        validateAlreadyReserved(name, date, timeId, themeId);
        Waiting saveWaiting = waitingRepository.save(waiting);
        return findWaitingWithNumber(saveWaiting.getId());
    }

    @Transactional
    public void removeWaiting(Long id, String userName) {
        Waiting waiting = findWaiting(id);
        waiting.validateCancelable(LocalDateTime.now(), userName);
        waitingRepository.deleteById(id);
    }

    private Waiting createWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        TimeSlot timeSlot = findTimeSlot(timeId);
        Theme theme = findTheme(themeId);
        return new Waiting(name, date, timeSlot, theme, LocalDateTime.now());
    }

    private void validateDuplicatedWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        if (waitingRepository.exists(name, date, timeId, themeId)) {
            throw new DuplicateWaitingException();
        }
    }

    private void validateAlreadyReserved(String name, LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByNameAndDateAndTimeAndTheme(name, date, timeId, themeId)) {
            throw new DuplicateReservationException();
        }
    }

    private Waiting findWaiting(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(WaitingNotFoundException::new);
    }

    private WaitingWithNumber findWaitingWithNumber(Long id) {
        return waitingRepository.findWaitingWithNumberById(id)
                .orElseThrow(WaitingNotFoundException::new);
    }

    private TimeSlot findTimeSlot(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(TimeSlotNotFoundException::new);
    }

    private Theme findTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(ThemeNotFoundException::new);
    }
}
