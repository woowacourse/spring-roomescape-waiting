package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.WaitingResult;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.ErrorCode;
import roomescape.service.exception.ResourceNotFoundException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public WaitingService(ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                          WaitingRepository waitingRepository, ReservationRepository reservationRepository,
                          Clock clock) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public WaitingResult createWaiting(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.THEME_NOT_FOUND));

        Waiting waiting = Waiting.create(name, date, time, theme, LocalDateTime.now(clock));

        checkDuplicatedWaiting(waiting);
        checkDuplicatedReservation(waiting);

        waiting = waitingRepository.save(waiting);
        Long order = waitingRepository.countByThemeIdAndDateAndTimeIdAndIdLessThanEqual(waiting.getId(),
                waiting.getTheme(), waiting.getDate(), waiting.getTime());

        return WaitingResult.of(waiting, order);
    }

    public void deleteWaiting(Long id, String name) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        waiting.validateOwner(name);
        waitingRepository.delete(waiting);
    }

    private void checkDuplicatedWaiting(Waiting waiting) {
        boolean duplicated = waitingRepository.existsByScheduleAndName(waiting.getDate(), waiting.getTime().getId(),
                waiting.getTheme().getId(), waiting.getName());

        if (duplicated) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_WAITING);
        }
    }

    private void checkDuplicatedReservation(Waiting waiting) {
        boolean duplicated = reservationRepository.findBySchedule(
                        waiting.getDate(),
                        waiting.getTime().getId(),
                        waiting.getTheme().getId())
                .filter(found -> waiting.isSameName(found.getName()))
                .isPresent();

        if (duplicated) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }
}
