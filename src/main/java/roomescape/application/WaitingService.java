package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.LoginMember;
import roomescape.application.dto.WaitingRequest;
import roomescape.application.dto.WaitingWithRankResponse;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingFactory;
import roomescape.domain.dto.WaitingWithRank;
import roomescape.domain.repository.WaitingCommandRepository;
import roomescape.domain.repository.WaitingQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingFactory waitingFactory;
    private final WaitingCommandRepository waitingCommandRepository;
    private final WaitingQueryRepository waitingQueryRepository;

    public WaitingService(WaitingFactory waitingFactory,
                          WaitingCommandRepository waitingCommandRepository,
                          WaitingQueryRepository waitingQueryRepository) {
        this.waitingFactory = waitingFactory;
        this.waitingCommandRepository = waitingCommandRepository;
        this.waitingQueryRepository = waitingQueryRepository;
    }

    @Transactional
    public List<WaitingWithRankResponse> reserveWaiting(LoginMember loginMember, WaitingRequest request) {
        Waiting waiting = waitingFactory.create(loginMember.id(), request.date(), request.timeId(), request.themeId());
        waitingCommandRepository.save(waiting);
        List<WaitingWithRank> waitingWithRanks = waitingQueryRepository.findWaitingWithRankByMemberId(loginMember.id());
        return convertToWaitWithRankResponses(waitingWithRanks);
        //TODO: ReservationFactory, WaitingFactory 다시 생각해봐야될듯
        // 그래야 서비스 우발적 중복 + waitingFactory 에서 중복 예약 대기 튕겨내는 로직
        // factory 내에서 수행할 수 있음(비즈니스 로직이 service 에 나와있지 않도록)
    }

    private List<WaitingWithRankResponse> convertToWaitWithRankResponses(List<WaitingWithRank> waitingWithRanks) {
        return waitingWithRanks.stream()
                .map(WaitingWithRankResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(Long waitingId) {
        if (!waitingQueryRepository.existsById(waitingId)) {
            throw new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_WAITING,
                    String.format("존재하지 않는 예약 대기입니다. 요청 예약 대기 id:%d", waitingId));
        }
        waitingCommandRepository.deleteById(waitingId);
    }
}
