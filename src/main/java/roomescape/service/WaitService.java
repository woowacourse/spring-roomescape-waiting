package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Wait;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.WaitRepository;

@Component
@Transactional(readOnly = true)
public class WaitService {

    public static final int MAX_WAITING_COUNT = 3;

    private final WaitRepository waitRepository;

    public WaitService(WaitRepository waitRepository) {
        this.waitRepository = waitRepository;
    }

    @Transactional
    public Wait save(Wait waitWithoutId) {
        List<Wait> waits = waitRepository.findBySlot(
                waitWithoutId.getReservationDate(),
                waitWithoutId.getTime().getId(),
                waitWithoutId.getTheme().getId());

        for (Wait wait : waits) {
            if (wait.getName().equals(waitWithoutId.getName())) {
                throw new CustomInvalidRequestException(ErrorCode.DUPLICATED_WAIT);
            }
        }

        if (waits.size() >= MAX_WAITING_COUNT) {
            throw new CustomInvalidRequestException(ErrorCode.WAIT_IS_FULL);
        }

        return waitRepository.save(waitWithoutId);
    }

    public List<Wait> findByName(String name) {
        return waitRepository.findByName(name);
    }

    public List<Wait> findAll() {
        return waitRepository.findAll();
    }

    public void delete(Long id) {
        waitRepository.delete(id);
    }

    public List<Wait> findBySlot(LocalDate reservationDate, Long timeId, Long themeId) {
        return waitRepository.findBySlot(reservationDate, timeId, themeId);
    }

    public Wait findWait(Long waitId) {
        return waitRepository.findById(waitId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_WAIT));
    }

    public Long calculateOrder(Wait wait) {
        return waitRepository.findOrderByWait(wait);
    }
}
