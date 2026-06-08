package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Wait;
import roomescape.domain.Waits;
import roomescape.exception.custom.CannotDeleteReservationTimeInUseException;
import roomescape.exception.custom.CannotDeleteThemeInUseException;
import roomescape.exception.custom.WaitNotExistsException;
import roomescape.repository.WaitRepository;
import roomescape.validator.WaitValidator;
import roomescape.validator.WaitValidatorFactory;

@Service
@Transactional(readOnly = true)
public class WaitService {

    private final WaitRepository waitRepository;
    private final Clock clock;

    public WaitService(WaitRepository waitRepository, Clock clock) {
        this.waitRepository = waitRepository;
        this.clock = clock;
    }

    @Transactional
    public Wait save(Wait waitWithoutId) {
        Waits waits = waitRepository.findBySlot(waitWithoutId.getReservationDate(), waitWithoutId.getTime().getId(),
                waitWithoutId.getTheme().getId());
        waits.validateCreate(waitWithoutId.getName());

        return waitRepository.save(waitWithoutId);
    }

    public Waits findByName(String name) {
        return waitRepository.findByName(name);
    }

    public Waits findAll() {
        return waitRepository.findAll();
    }

    @Transactional
    public void delete(Long id, boolean isAdmin) {
        Wait wait = waitRepository.findById(id)
                .orElseThrow(WaitNotExistsException::new);
        WaitValidator waitValidator = WaitValidatorFactory.getValidator(isAdmin);
        waitValidator.validateDelete(wait, LocalDateTime.now(clock));
        waitRepository.deleteById(id);
    }

    public Waits findBySlot(LocalDate localDate, Long timeId, Long themeId) {
        return waitRepository.findBySlot(localDate, timeId, themeId);
    }

    public Wait findWait(Long waitId) {
        return waitRepository.findById(waitId)
                .orElseThrow(WaitNotExistsException::new);
    }

    public Long calculateOrder(Wait wait) {
        return waitRepository.findOrderByWait(wait);
    }

    public void validateReferencedTime(Long timeId) {
        if (waitRepository.existsByTimeId(timeId)) {
            throw new CannotDeleteReservationTimeInUseException();
        }
    }

    public void validateReferencedTheme(Long themeId) {
        if (waitRepository.existsByThemeId(themeId)) {
            throw new CannotDeleteThemeInUseException();
        }
    }
}
