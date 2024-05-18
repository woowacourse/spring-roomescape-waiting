package roomescape.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.application.dto.LoginMember;
import roomescape.application.dto.WaitingRequest;
import roomescape.application.dto.WaitingWithRankResponse;
import roomescape.domain.Member;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.WaitingFactory;
import roomescape.domain.dto.WaitingWithRank;
import roomescape.domain.repository.MemberQueryRepository;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.ThemeQueryRepository;
import roomescape.domain.repository.TimeQueryRepository;
import roomescape.domain.repository.WaitingCommandRepository;
import roomescape.domain.repository.WaitingQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class WaitingService {

    private final WaitingFactory waitingFactory;
    private final ReservationQueryRepository reservationQueryRepository;
    private final TimeQueryRepository timeQueryRepository;
    private final ThemeQueryRepository themeQueryRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final WaitingCommandRepository waitingCommandRepository;
    private final WaitingQueryRepository waitingQueryRepository;
    private final Clock clock;

    public WaitingService(WaitingFactory waitingFactory,
                          ReservationQueryRepository reservationQueryRepository,
                          TimeQueryRepository timeQueryRepository,
                          ThemeQueryRepository themeQueryRepository,
                          MemberQueryRepository memberQueryRepository,
                          WaitingCommandRepository waitingCommandRepository,
                          WaitingQueryRepository waitingQueryRepository,
                          Clock clock) {
        this.waitingFactory = waitingFactory;
        this.reservationQueryRepository = reservationQueryRepository;
        this.timeQueryRepository = timeQueryRepository;
        this.themeQueryRepository = themeQueryRepository;
        this.memberQueryRepository = memberQueryRepository;
        this.waitingCommandRepository = waitingCommandRepository;
        this.waitingQueryRepository = waitingQueryRepository;
        this.clock = clock;
    }

    public List<WaitingWithRankResponse> reserveWaiting(LoginMember loginMember, WaitingRequest request) {
        LocalDate date = request.date();
        Time time = timeQueryRepository.getById(request.timeId());
        Theme theme = themeQueryRepository.getById(request.themeId());
        if (!reservationQueryRepository.existsByDateAndTimeAndTheme(date, time, theme)) {
            throw new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_RESERVATION);
        }
        Member member = memberQueryRepository.getById(loginMember.id());
        waitingCommandRepository.save(waitingFactory.create(member, date, time, theme, clock));
        List<WaitingWithRank> waitingWithRanks = waitingQueryRepository.findWaitingWithRankByMemberId(member.getId());
        return convertToWaitWithRankResponses(waitingWithRanks);
    }

    private List<WaitingWithRankResponse> convertToWaitWithRankResponses(List<WaitingWithRank> waitingWithRanks) {
        return waitingWithRanks.stream()
                .map(WaitingWithRankResponse::from)
                .toList();
    }

    public void deleteById(Long waitingId) {
        if (!waitingQueryRepository.existsById(waitingId)) {
            throw new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_WAITING,
                    String.format("존재하지 않는 예약 대기입니다. 요청 예약 대기 id:%d", waitingId));
        }
        waitingCommandRepository.deleteById(waitingId);
    }
}
