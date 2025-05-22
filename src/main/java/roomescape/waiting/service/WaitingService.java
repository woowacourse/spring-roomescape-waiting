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
//        long rank = calculateWaitingRank(savedWaiting);

        return WaitingResponse.from(savedWaiting);
    }

//    private long calculateWaitingRank(Waiting waiting) {
//        return 0;
//    }

    public void confirmWaiting(Long waitingId) {
        // 1. 대기 정보 조회
        // 2. 예약 생성
        // 3. 대기 상태 업데이트
    }

    public void cancelWaiting(Long waitingId) {
        // 1. 대기 취소
        // 2. 다음 대기자에게 알림
    }
}
