package roomescape.reservation.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.WaitingCreateRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Waiting save(
            final WaitingCreateRequest request,
            final LoginMemberInToken loginMember
    ) {
        Waiting waiting = getValidatedWaiting(request, loginMember);

        return waitingRepository.save(waiting);
    }

    private Waiting getValidatedWaiting(
            final WaitingCreateRequest request,
            final LoginMemberInToken loginMember
    ) {
        ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        final Waiting waiting = request.toWaiting(member, theme, reservationTime);
        validateWaiting(waiting);
        return waiting;
    }

    private void validateWaiting(Waiting waiting) {
        Reservation reservation = reservationRepository.findByThemeIdAndDateAndReservationTimeStartAt(
                        waiting.getTheme().getId(), waiting.getDate(), waiting.getReservationTime().getStartAt())
                .orElseThrow(() -> new IllegalArgumentException("예약이 없어 예약 대기를 할 수 없습니다."));

        final Long memberId = waiting.getMember().getId();
        final Long reservationMemberId = reservation.getMember().getId();
        if (memberId.equals(reservationMemberId)) {
            throw new IllegalArgumentException("이미 예약 중 입니다.");
        }

        if (waitingRepository.existsByThemeIdAndDateAndReservationTimeStartAt(
                waiting.getTheme().getId(), waiting.getDate(), waiting.getReservationTime().getStartAt())) {
            throw new IllegalArgumentException("이미 예약 대기 중 입니다.");
        }
    }

    public List<WaitingResponse> findAll(final LoginMemberInToken loginMember) {
        if (!loginMember.role().isAdmin()) {
            throw new IllegalArgumentException("관리자만 조회할 수 있습니다.");
        }

        List<WaitingResponse> waitingResponses = new ArrayList<>();

        for (final Waiting waiting : waitingRepository.findAll()) {
            waitingResponses.add(new WaitingResponse(waiting));
        }

        return waitingResponses;
    }

    public List<MyReservationResponse> findWaitingWithRanksByMemberId(Long memberId) {
        return waitingRepository.findWaitingWithRanksByMemberId(memberId).stream()
                .map(MyReservationResponse::new)
                .toList();
    }

    public void delete(final Long id, final LoginMemberInToken loginMemberInToken) {
        final Waiting target = waitingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 대기 입니다."));

        final Long MemberIdOfTarget = target.getMember().getId();
        if (!loginMemberInToken.role().isAdmin() && !MemberIdOfTarget.equals(loginMemberInToken.id())) {
            throw new IllegalArgumentException("본인의 예약 대기만 취소할 수 있습니다.");
        }

        waitingRepository.delete(target);
    }
}
