package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Wait;
import roomescape.domain.Waits;
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
        Waits waits = waitRepository.findBySlot(
                waitWithoutId.getReservationDate(),
                waitWithoutId.getTime().getId(),
                waitWithoutId.getTheme().getId());

        waits.validateAddable(waitWithoutId);

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

    public Waits findBySlot(LocalDate reservationDate, Long timeId, Long themeId) {
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
