package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Wait;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.repository.WaitRepository;

@Service
public class WaitService {

    private final WaitRepository waitRepository;

    public WaitService(WaitRepository waitRepository) {
        this.waitRepository = waitRepository;
    }

    public Wait save(Wait waitWithoutId) {
        List<Wait> waits = waitRepository.findBySlot(
                waitWithoutId.getReservationDate(),
                waitWithoutId.getTime().getId(),
                waitWithoutId.getTheme().getId());

        for (Wait wait : waits) {
            if (wait.isWaitedBy(waitWithoutId.getName())) {
                throw new RoomEscapeException(DomainErrorCode.DUPLICATED_WAIT);
            }
        }

        if (waits.size() >= 3) {
            throw new RoomEscapeException(DomainErrorCode.WAIT_IS_FULL);
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
                .orElseThrow(() -> new RoomEscapeException(DomainErrorCode.NOT_FOUND_WAIT));
    }

    public Long calculateOrder(Wait wait) {
        return waitRepository.findOrderByWait(wait);
    }
}
