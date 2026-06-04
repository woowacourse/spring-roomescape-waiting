package roomescape.waiting.application;

import java.time.LocalDate;
import java.util.Optional;
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
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingReference waitingReference;
    private final WaitingValidator waitingValidator;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            WaitingReference waitingReference,
            WaitingValidator waitingValidator
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingReference = waitingReference;
        this.waitingValidator = waitingValidator;
    }

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
        return waitingRepository.save(waiting);
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
                    waitingRepository.deleteByIdAndName(waiting.getId(), waiting.getName());
                    waitingReference.promoteToReservation(waiting);
                }
        );
    }

    @Transactional
    public void cancelWaiting(Long id, String name) {
        Waiting targetWaiting = waitingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(WaitingErrorCode.WAITING_NOT_FOUND, id));
        targetWaiting.cancel(name);
        waitingRepository.deleteByIdAndName(id, name);
    }
}
