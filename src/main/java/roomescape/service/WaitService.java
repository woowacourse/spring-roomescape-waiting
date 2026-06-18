package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Slot;
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
        Waits waits = new Waits(
                waitRepository.findBySlot(waitWithoutId.getReservationDate(), waitWithoutId.getTimeId(),
                        waitWithoutId.getThemeId())
        );
        waits.validateCreate(waitWithoutId.getMember(), waitWithoutId.getSlot());

        return waitRepository.save(waitWithoutId);
    }

    public Waits findByMemberId(Long memberId) {
        return new Waits(waitRepository.findByMemberId(memberId));
    }

    public Waits findByName(String name) {
        return new Waits(waitRepository.findByMember_Name(name));
    }

    public Waits findAll() {
        return new Waits(waitRepository.findAllWaits());
    }

    @Transactional
    public void delete(Long id, boolean isAdmin) {
        Wait wait = waitRepository.findById(id)
                .orElseThrow(WaitNotExistsException::new);
        WaitValidator waitValidator = WaitValidatorFactory.getValidator(isAdmin);
        waitValidator.validateDelete(wait, LocalDateTime.now(clock));
        waitRepository.deleteById(id);
    }

    public Waits findBySlot(Slot slot) {
        return new Waits(waitRepository.findBySlot(slot.getReservationDate(), slot.getTimeId(),
                slot.getThemeId()));
    }

    public Wait findWait(Long waitId) {
        return waitRepository.findById(waitId)
                .orElseThrow(WaitNotExistsException::new);
    }

    public Long calculateOrder(Wait wait) {
        return waitRepository.calculateWaitingOrder(wait.getReservationDate(), wait.getTimeId(), wait.getThemeId(),
                wait.getId());
    }

    public void validateReferencedTime(Long timeId) {
        if (waitRepository.existsBySlot_Time_Id(timeId)) {
            throw new CannotDeleteReservationTimeInUseException();
        }
    }

    public void validateReferencedTheme(Long themeId) {
        if (waitRepository.existsBySlot_Theme_Id(themeId)) {
            throw new CannotDeleteThemeInUseException();
        }
    }
}
