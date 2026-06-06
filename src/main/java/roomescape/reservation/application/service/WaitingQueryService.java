package roomescape.reservation.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.presentation.dto.WaitingResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitingQueryService {

    private final WaitingRepository waitingRepository;

    @Transactional(readOnly = true)
    public List<WaitingResponse> findAllByName(String name) {
        return waitingRepository.findByName(name).stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
