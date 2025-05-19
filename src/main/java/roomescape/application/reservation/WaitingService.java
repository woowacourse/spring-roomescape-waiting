package roomescape.application.reservation;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.CreateWaitingParam;
import roomescape.application.reservation.dto.WaitingResult;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.repository.ThemeRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.repository.WaitingRepository;
import roomescape.domain.reservation.WaitingWithRank;
import roomescape.infrastructure.error.exception.ReservationException;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationRepository reservationRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository,
                          MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Long create(CreateWaitingParam createParameter) {
        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(
                createParameter.reservationDate(),
                createParameter.timeId(),
                createParameter.themeId())) {
            throw new ReservationException("예약이 존재하지 않아 대기를 신청할 수 없습니다. 바로 예약을 진행해주세요.");
        }
        ReservationTime reservationTime = reservationTimeRepository.findById(createParameter.timeId())
                .orElseThrow(() -> new ReservationException("예약 시간 정보가 존재하지 않습니다."));
        Theme theme = themeRepository.findById(createParameter.themeId())
                .orElseThrow(() -> new ReservationException("테마 정보가 존재하지 않습니다."));
        Member member = memberRepository.findById(createParameter.memberId())
                .orElseThrow(() -> new ReservationException("회원 정보가 존재하지 않습니다."));
        Waiting waiting = waitingRepository.save(new Waiting(
                        member,
                        createParameter.reservationDate(),
                        reservationTime,
                        theme
                )
        );
        return waiting.getId();
    }

    public List<WaitingResult> findWaitingByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ReservationException("회원 정보가 존재하지 않습니다."));
        List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(member.getId());
        return waitings.stream()
                .map(waitingWithRank -> new WaitingWithRank(waitingWithRank.waiting(), waitingWithRank.rank() + 1))
                .map(waitingWithRank -> WaitingResult.from(waitingWithRank.waiting(), waitingWithRank.rank()))
                .toList();
    }

    @Transactional
    public void cancel(Long waitingId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ReservationException("회원 정보가 존재하지 않습니다."));
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new ReservationException("대기 정보가 존재하지 않습니다."));
        if (!waiting.isOwner(member)) {
            throw new ReservationException("대기 취소 권한이 없습니다.");
        }
        waitingRepository.delete(waiting);
    }
}
