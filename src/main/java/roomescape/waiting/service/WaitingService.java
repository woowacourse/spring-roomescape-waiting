package roomescape.waiting.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.WaitingCreateRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository, MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public WaitingResponse createWaiting(WaitingCreateRequest request, Long waitingMemberId) {
        Reservation reservation =
                findReservationByDateAndTimeAndTheme(request.date(), request.timeId(), request.themeId());
        Member waitingMember = findMemberByMemberId(waitingMemberId);

        Waiting waiting = request.createWaiting(reservation, waitingMember);
        Waiting createdWaiting = waitingRepository.save(waiting);

        return WaitingResponse.from(createdWaiting);
    }

    private Reservation findReservationByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findByDateAndTime_idAndTheme_id(date, timeId, themeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약에 대해 대기할 수 없습니다."));
    }

    private Member findMemberByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버가 존재하지 않습니다."));
    }
}
