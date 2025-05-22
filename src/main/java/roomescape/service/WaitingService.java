package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingValidator;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundException;
import roomescape.service.param.CreateBookingParam;
import roomescape.service.result.WaitingResult;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final WaitingValidator waitingValidator;

    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository, WaitingValidator waitingValidator,
                          MemberRepository memberRepository, ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.waitingValidator = waitingValidator;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<WaitingResult> getAll() {
        List<Waiting> waitings = waitingRepository.findAll();
        return WaitingResult.from(waitings);
    }

    @Transactional
    public WaitingResult create(CreateBookingParam param) {
        WaitingComponents components = loadComponents(param);
        Waiting waiting = Waiting.create(
                components.member, param.date(), components.time, components.theme);
        waitingValidator.validateCanWaiting(waiting);
        Waiting saved = waitingRepository.save(waiting);

        return WaitingResult.from(saved);
    }

    private WaitingComponents loadComponents(CreateBookingParam param) {
        ReservationTime reservationTime = reservationTimeRepository.findById(param.timeId())
                .orElseThrow(() -> new NotFoundException("timeId", param.timeId()));
        Theme theme = themeRepository.findById(param.themeId())
                .orElseThrow(() -> new NotFoundException("themeId", param.themeId()));
        Member member = memberRepository.findById(param.memberId())
                .orElseThrow(() -> new NotFoundException("memberId", param.memberId()));

        return new WaitingComponents(member, theme, reservationTime);
    }

    private record WaitingComponents(Member member, Theme theme, ReservationTime time) {
    }

    @Transactional
    public void denyWaitingById(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new NotFoundException("waitingId", waitingId));
        waitingRepository.delete(waiting);
    }

    @Transactional
    public void cancelWaitingById(Long waitingId, LoginMemberInfo loginMemberInfo) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new NotFoundException("waitingId", waitingId));
        validateCancelPermission(loginMemberInfo, waiting);

        waitingRepository.delete(waiting);
    }

    private void validateCancelPermission(LoginMemberInfo loginMemberInfo, Waiting waiting) {
        boolean notSameMember = !waiting.sameWaiterWith(loginMemberInfo.id());
        if (notSameMember) {
            throw new DeletionNotAllowedException("자신의 예약만 삭제할 수 있습니다.");
        }
    }
}
