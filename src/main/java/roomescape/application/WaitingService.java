package roomescape.application;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.WaitingServiceResponse;
import roomescape.domain.ReservationStatus;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Waiting;
import roomescape.domain.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final GameScheduleService gameScheduleService;
    private final MemberService memberService;

    public WaitingService(
            WaitingRepository waitingRepository,
            GameScheduleService gameScheduleService,
            MemberService memberService
    ) {
        this.waitingRepository = waitingRepository;
        this.gameScheduleService = gameScheduleService;
        this.memberService = memberService;
    }

    @Transactional
    public WaitingServiceResponse registerWaiting(ReservationCreateServiceRequest request) {
        GameSchedule gameSchedule = gameScheduleService.getGameScheduleEntityBy(
                request.date(),
                request.timeId(),
                request.themeId(),
                LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        );
        Member member = memberService.getMemberEntityById(request.memberId());

        Waiting waitingWithoutId = Waiting.withoutId(member, gameSchedule, ReservationStatus.WAITING);
        Waiting waiting = waitingRepository.save(waitingWithoutId);
        return WaitingServiceResponse.from(waiting);
    }
}
