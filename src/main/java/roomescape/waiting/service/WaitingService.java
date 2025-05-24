package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;
import roomescape.waiting.service.dto.request.CreateWaitingRequest;
import roomescape.waiting.service.dto.response.CreateWaitingResponse;

import java.time.LocalDateTime;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ThemeRepository themeRepository,
            ReservationTimeRepository timeRepository,
            MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.memberRepository = memberRepository;
    }

    public void delete(final Long id, LoginMember loginMember) {
        Waiting waiting = waitingRepository.findByIdAndMemberId(id, loginMember.id())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 대기 예약 건입니다."));
        waitingRepository.delete(waiting);
    }

    public CreateWaitingResponse createWaiting(CreateWaitingRequest request, LoginMember loginMember) {
        Member member = getMember(loginMember.id());
        ReservationTime time = getReservationTime(request.timeId());
        Theme theme = getTheme(request.themeId());
        Waiting waiting = new Waiting(request.date(), time, theme, member, LocalDateTime.now());

        Waiting saved = waitingRepository.save(waiting);

        return CreateWaitingResponse.from(saved);
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 회원입니다."));
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("%d 식별자를 갖는 테마가 존재하지 않습니다.", themeId)));
    }

    private ReservationTime getReservationTime(final Long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("%d 식별자를 갖는 테마가 존재하지 않습니다.", timeId)));
    }
}
