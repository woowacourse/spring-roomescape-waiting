package roomescape.waiting.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.reservation.presentation.dto.response.MyReservationResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.repository.WaitingRepository;

@Service
public class WaitingDomainService {

    private final WaitingRepository waitingRepository;

    public WaitingDomainService(final WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public Waiting save(final Waiting waiting) {
        return waitingRepository.save(waiting);
    }

    public List<MyReservationResponse> findMyReservations(final MemberInfo memberInfo) {
        return waitingRepository.findByWaitingMemberId(memberInfo.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
