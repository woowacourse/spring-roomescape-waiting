package roomescape.service.waiting;

import org.springframework.stereotype.Service;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.dto.reservation.ReservationAndWaitingResponseDto;
import roomescape.dto.waiting.WaitingResponseDto;
import roomescape.repository.JpaWaitingRepository;

import java.util.List;

@Service
public class WaitingQueryService {

    JpaWaitingRepository waitingRepository;

    public WaitingQueryService(JpaWaitingRepository jpaWaitingRepository) {
        this.waitingRepository = jpaWaitingRepository;
    }

    public List<ReservationAndWaitingResponseDto> findMyWaiting(long memberId){
        List<WaitingWithRank> byMemberId = waitingRepository.findByMemberId(memberId);
        return byMemberId.stream().map(ReservationAndWaitingResponseDto::new).toList();
    }

    public List<WaitingResponseDto> findAllWaiting(){
        List<Waiting> all = waitingRepository.findAll();
        return all.stream().map(waiting -> WaitingResponseDto.from(waiting)).toList();
    }
}
