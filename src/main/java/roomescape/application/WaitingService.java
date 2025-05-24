package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.AuthorizationException;
import roomescape.exception.NotFoundException;

@Service
@Transactional
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final MemberService memberService;

    public WaitingService(ReservationRepository reservationRepository, MemberService memberService) {
        this.reservationRepository = reservationRepository;
        this.memberService = memberService;
    }

    public long countWaitingReservation(Reservation reservation) {
        if (!reservation.isWaiting()) {
            throw new IllegalArgumentException("예약 대기 상태가 아닙니다.");
        }
        return reservationRepository.countByReservationWaitingOrderByCreatedAt(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getWaiting().getSavedDateTime()
        );
    }

    public void deleteWaiting(Long reservationId, Long memberId) {
        Member member = memberService.getMemberEntityById(memberId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Id에 해당하는 예약이 없습니다."));
        if (!member.isAdmin() && !member.isSame(reservation.getMember())) {
            throw new AuthorizationException("권한이 없습니다.");
        }
        reservation.cancel();
    }
}
