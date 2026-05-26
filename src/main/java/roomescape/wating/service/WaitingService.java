package roomescape.wating.service;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.wating.repository.WaitingRepository;
import roomescape.wating.service.dto.request.WaitingCreateRequest;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final Clock clock;

    public long create(final WaitingCreateRequest request) {

        return 1L;
    }
}
