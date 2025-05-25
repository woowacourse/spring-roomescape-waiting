package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ReservationDto;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
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

    @Transactional(readOnly = true)
    public List<ReservationDto> getAllWaitings() {
        List<Reservation> waitings = reservationRepository.findByWaitingStatus(ReservationStatus.WAITING);
        return ReservationDto.from(waitings);
    }

    @Transactional(readOnly = true)
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

    public void acceptReserve(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("삭제하려는 예약 id가 존재하지 않습니다. id: " + reservationId));
        if (!reservation.isWaiting()) {
            throw new IllegalArgumentException("해당 예약은 대기 상태가 아닙니다.");
        }
        if (isDateTimeAlreadyReserved(reservation)) {
            throw new IllegalArgumentException("해당 일시의 예약이 이미 존재합니다.");
        }
        reservation.reserve();
    }

    private boolean isDateTimeAlreadyReserved(Reservation reservation) {
        return reservationRepository.existsByDateAndTimeIdAndThemeIdAndWaitingStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                ReservationStatus.RESERVED
        );
    }

    public void deleteWaiting(Long reservationId, Long memberId) {
        Member member = memberService.getMemberEntityById(memberId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("삭제하려는 예약 id가 존재하지 않습니다. id: " + reservationId));
        if (!member.isAdmin() && !member.isSame(reservation.getMember())) {
            throw new AuthorizationException("권한이 없습니다.");
        }
        reservation.deleteSelf();
        reservationRepository.delete(reservation);
    }
}
