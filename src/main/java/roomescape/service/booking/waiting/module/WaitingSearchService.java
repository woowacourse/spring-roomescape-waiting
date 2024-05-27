package roomescape.service.booking.waiting.module;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingSearchService {

    private final WaitingRepository waitingRepository;

    public WaitingSearchService(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public List<WaitingResponse> findAllWaitingReservations() {
        return waitingRepository.findAll()
                .stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
