package roomescape.application;

import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.LoginMember;
import roomescape.application.dto.WaitingRequest;
import roomescape.application.dto.WaitingResponse;
import roomescape.application.dto.WaitingWithRankResponse;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingFactory;
import roomescape.domain.dto.WaitingWithRankDto;
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
    private final Clock clock;

    public WaitingService(WaitingFactory waitingFactory,
                          WaitingCommandRepository waitingCommandRepository,
                          WaitingQueryRepository waitingQueryRepository,
                          Clock clock) {
        this.waitingFactory = waitingFactory;
        this.waitingCommandRepository = waitingCommandRepository;
        this.waitingQueryRepository = waitingQueryRepository;
        this.clock = clock;
    }

    @Transactional
    public List<WaitingWithRankResponse> reserveWaiting(LoginMember loginMember, WaitingRequest request) {
        Waiting waiting = waitingFactory.create(loginMember.id(), request.date(), request.timeId(), request.themeId());
        waitingCommandRepository.save(waiting);
        List<WaitingWithRankDto> waitingWithRankDtos = waitingQueryRepository.findWaitingWithRankByMemberId(loginMember.id());
        return convertToWaitWithRankResponses(waitingWithRankDtos);
    }

    private List<WaitingWithRankResponse> convertToWaitWithRankResponses(List<WaitingWithRankDto> waitingWithRankDtos) {
        return waitingWithRankDtos.stream()
                .map(WaitingWithRankResponse::from)
                .toList();
    }

    @Transactional
    public void cancel(Long waitingId) {
        Waiting waiting = waitingQueryRepository.getById(waitingId);
        if (waiting.isPast(clock)) {
            throw new RoomescapeException(RoomescapeErrorCode.DATE_EXPIRED);
        }
        waitingCommandRepository.delete(waiting);
    }

    public List<WaitingResponse> findAll() {
        return waitingQueryRepository.findAll().stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
