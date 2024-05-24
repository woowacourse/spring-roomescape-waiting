package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.ValidationException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.WaitingResponse;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.dto.WaitingRequest;
import roomescape.reservation.repository.WaitingJpaRepository;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

import java.util.List;
import java.util.Optional;

@Service
public class WaitingService {

    private final WaitingJpaRepository waitingJpaRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;

    public WaitingService(WaitingJpaRepository waitingJpaRepository,
                          ReservationTimeService reservationTimeService,
                          ThemeService themeService,
                          MemberService memberService
    ) {
        this.waitingJpaRepository = waitingJpaRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
    }

    public ReservationResponse addWaiting(WaitingRequest waitingRequest, MemberRequest memberRequest) {
        ReservationTimeResponse timeResponse = reservationTimeService.getTime(waitingRequest.time());
        ThemeResponse themeResponse = themeService.getTheme(waitingRequest.theme());
        Member member = memberService.getLoginMemberById(memberRequest.id());
        Waiting waiting = new Waiting(waitingRequest.date(),
                timeResponse.toReservationTime(),
                themeResponse.toTheme(),
                member);
        validateIsBeforeNow(waiting);
        validateIsDuplicated(waiting);
        Waiting savedWaiting = waitingJpaRepository.save(waiting);
        return new ReservationResponse(savedWaiting);
    }

    private void validateIsBeforeNow(Waiting waiting) {
        if (waiting.isBeforeNow()) {
            throw new ValidationException("과거 시간은 예약할 수 없습니다.");
        }
    }

    private void validateIsDuplicated(Waiting waiting) {
        if (waitingJpaRepository.existsByDateAndReservationTimeAndThemeAndMember(
                waiting.getDate(),
                waiting.getReservationTime(),
                waiting.getTheme(),
                waiting.getMember())
        ) {
            throw new DuplicationException("이미 예약 대기중 입니다.");
        }
    }

    public List<WaitingWithRank> findWaitingsByMember(Member loginMember) {
        return waitingJpaRepository.findWaitingsWithRankByMemberId(loginMember.getId());
    }

    public void deleteById(Long id) {
        waitingJpaRepository.deleteById(id);
    }

    public List<WaitingResponse> findWaitings() {
        return waitingJpaRepository.findAll()
                .stream()
                .map(WaitingResponse::new)
                .toList();
    }

    public Optional<Waiting> findWaitingByReservation(Reservation reservation) {
        return waitingJpaRepository.findTopByDateAndReservationTimeAndTheme(
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme()
        );
    }
}
