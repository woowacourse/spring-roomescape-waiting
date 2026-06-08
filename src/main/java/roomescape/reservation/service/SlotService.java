package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.repository.SlotRepository;

@Service
public class SlotService {

    private final SlotRepository slotRepository;

    public SlotService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensure(Long themeId, Long timeId) {
        slotRepository.ensure(themeId, timeId);
    }

    @Transactional
    public void lock(Long themeId, Long timeId) {
        slotRepository.lock(themeId, timeId);
    }
}
