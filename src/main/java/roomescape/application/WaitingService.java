package roomescape.application;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.WaitingServiceResponse;
import roomescape.domain.ReservationStatus;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Waiting;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.NotFoundException;

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
        GameSchedule gameSchedule = getGameScheduleBy(request.date(), request.timeId(), request.themeId());
        Member member = memberService.getMemberEntityById(request.memberId());
        validateNotDuplicate(gameSchedule, member);

        Waiting waitingWithoutId = Waiting.withoutId(member, gameSchedule, ReservationStatus.WAITING);
        Waiting waiting = waitingRepository.save(waitingWithoutId);
        return WaitingServiceResponse.from(waiting);
    }

    private GameSchedule getGameScheduleBy(LocalDate date, Long timeId, Long themeId) {
        try {
            return gameScheduleService.getGameScheduleEntityBy(date, timeId, themeId);
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("예약대기를 신청할 수 없습니다. 예약하기를 이용해주세요.");
        }
    }

    private void validateNotDuplicate(GameSchedule gameSchedule, Member member) {
        boolean duplicated = waitingRepository.existsByGameScheduleIdAndMemberId(gameSchedule.getId(), member.getId());
        if (duplicated) {
            throw new IllegalArgumentException("예약대기는 한 번만 신청할 수 있습니다.");
        }
    }
}
