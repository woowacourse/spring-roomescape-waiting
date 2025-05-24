package roomescape.reservation.waiting.service;

import org.springframework.stereotype.Service;
import roomescape.global.auth.LoginMember;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.waiting.domain.Waiting;
import roomescape.reservation.waiting.dto.CreateWaitingRequest;
import roomescape.reservation.waiting.dto.WaitingResponse;
import roomescape.reservation.waiting.repository.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

import java.util.List;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public WaitingResponse createWaiting(final CreateWaitingRequest request) {
        final ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new BadRequestException("예약 시간이 존재하지 않습니다."));
        final Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new BadRequestException("예약자를 찾을 수 없습니다."));
        final Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new BadRequestException("테마가 존재하지 않습니다."));
        Waiting waiting = Waiting.register(request.date(), time, member, theme);
        Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingResponse(savedWaiting);
    }

    public List<WaitingResponse> getByMemberId(LoginMember loginMember) {
        List<Waiting> waitings = waitingRepository.findByMemberId(loginMember.id());
        return waitings.stream()
                .map(WaitingResponse::new)
                .toList();
    }

    public void cancelWaitingById(long id) {
        waitingRepository.deleteById(id);
    }
}

