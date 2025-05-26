package roomescape.waiting.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.dto.request.LoginMember;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.WaitingStatus;
import roomescape.waiting.dto.request.WaitingRequest;
import roomescape.waiting.dto.response.WaitingResponse;

@Service
public class WaitingService {

    private static final long MAX_WAITING_COUNT = 10;
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository, MemberRepository memberRepository, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public WaitingResponse createWaiting(WaitingRequest request, LoginMember loginMember) {
        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new IllegalArgumentException("예약 시간을 찾을 수 없습니다"));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new IllegalArgumentException("테마를 찾을 수 없습니다"));

        long currentWaitingCounts = waitingRepository.countByDateAndThemeIdAndTimeIdAndStatus(
                request.date(),
                request.themeId(),
                request.timeId(),
                WaitingStatus.PENDING
        );

        if (currentWaitingCounts >= MAX_WAITING_COUNT) {
            throw new IllegalStateException("최대 대기 인원(10명)을 초과했습니다.");
        }
        Waiting waiting = Waiting.createWithoutId(
                member,
                request.date(),
                time,
                theme,
                WaitingStatus.PENDING,
                LocalDateTime.now()
        );
        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    public void cancelWaiting(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("대기 정보를 찾을 수 없습니다."));
        waiting.cancel();
        waitingRepository.save(waiting);
    }
}
