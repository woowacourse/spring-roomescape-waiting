package roomescape.service.waiting;

import org.springframework.stereotype.Service;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.repository.JpaWaitingRepository;

import java.util.List;
import java.util.stream.Stream;

@Service
public class WaittingQueryService {

    JpaWaitingRepository waitingRepository;

    public WaittingQueryService(JpaWaitingRepository jpaWaitingRepository) {
        this.waitingRepository = jpaWaitingRepository;
    }

    public List<MyReservationResponseDto> findMyWaiting(long memberId){
        List<WaitingWithRank> byMemberId = waitingRepository.findByMemberId(memberId);
        return byMemberId.stream().map(MyReservationResponseDto::new).toList();
    }
}
