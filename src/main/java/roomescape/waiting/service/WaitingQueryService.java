package roomescape.waiting.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.waiting.controller.response.WaitingInfoResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WaitingQueryService {

    private final WaitingRepository waitingRepository;

    public List<WaitingInfoResponse> getAllInfo() {
        return waitingRepository.getAll();
    }

    public Waiting getWaiting(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 대기입니다."));
    }

    public List<MyReservationResponse> getWaitingReservations(Long memberId) {
        return waitingRepository.findWithRankByMemberId(memberId)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
