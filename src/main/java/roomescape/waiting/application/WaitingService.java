package roomescape.waiting.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingReference waitingReference;
    private final WaitingValidator waitingValidator;

    @Transactional
    public Waiting save(WaitingCreateCommand command) {
        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> new BusinessException(WaitingErrorCode.WAITING_TIME_INVALID));
        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(() -> new BusinessException(WaitingErrorCode.WAITING_THEME_INVALID));

        waitingReference.validateExistReservation(command);
        waitingValidator.validateAlreadyMyWaiting(command);
        Waiting waiting = Waiting.create(
                command.name(),
                command.date(),
                time,
                theme
        );
        try {
            return waitingRepository.save(waiting);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(WaitingErrorCode.WAITING_ALREADY_EXISTS);
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    public void promoteNextWaiting(
            LocalDate date,
            ReservationTime time,
            Theme theme) {
        Optional<Waiting> targetWaiting = waitingRepository.findFirstByDateAndTimeIdAndThemeId(
                date,
                time.getId(),
                theme.getId()
        );
        targetWaiting.ifPresent(waiting -> {
                    boolean deleted = waitingRepository.deleteByIdAndName(waiting.getId(), waiting.getName());
                    if (!deleted) {
                        return;
                    }
                    waitingReference.promoteToReservation(waiting);
                    log.info("대기 예약 승격이 완료되었습니다. waitingId={}, date={}, timeId={}, themeId={}",
                            waiting.getId(),
                            date,
                            time.getId(),
                            theme.getId());
                }
        );
    }

    @Transactional
    public void cancelWaiting(Long id, String name) {
        Waiting targetWaiting = waitingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(WaitingErrorCode.WAITING_NOT_FOUND, id));
        targetWaiting.cancel(name);
        boolean deleted = waitingRepository.deleteByIdAndName(id, name);
        if (!deleted) {
            throw new EntityNotFoundException(WaitingErrorCode.WAITING_NOT_FOUND, id);
        }
    }

    @Transactional
    public void promoteWaitingWithoutReservation() {
        List<Waiting> waitings = waitingRepository.findFirstWaitingsWithoutReservation();

        for (Waiting waiting : waitings) {
            promoteNextWaiting(
                    waiting.getDate(),
                    waiting.getTime(),
                    waiting.getTheme()
            );
        }
    }
}
