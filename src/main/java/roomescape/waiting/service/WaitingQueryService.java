package roomescape.waiting.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.waiting.controller.response.WaitingInfoResponse;
import roomescape.waiting.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WaitingQueryService {

    private final WaitingRepository waitingRepository;

    @Transactional(readOnly = true)
    public List<WaitingInfoResponse> getAllInfo() {
        return waitingRepository.getAll();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> getWaitingReservations(Long memberId) {
        return waitingRepository.findWithRankByMemberId(memberId)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
