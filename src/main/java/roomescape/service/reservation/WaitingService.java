package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.exception.ForbiddenException;
import roomescape.exception.InvalidReservationException;
import roomescape.service.reservation.dto.ReservationResponse;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WaitingService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public WaitingService(ReservationRepository reservationRepository, MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAllByStatus(ReservationStatus.WAITING).stream()
                .map(ReservationResponse::new)
                .toList();
    }

    @Transactional
    public void deleteWaitingById(long reservationId, long memberId) {
        reservationRepository.findById(reservationId)
                .ifPresent(reservation -> {
                    validateAuthority(reservation, memberId);
                    validateStatus(reservation);
                });
        reservationRepository.deleteById(reservationId);
    }

    private void validateAuthority(Reservation reservation, long memberId) {
        memberRepository.findById(memberId).ifPresent(member -> {
            if (member.isGuest() && !reservation.isReservationOf(memberId)) {
                throw new ForbiddenException("예약 대기를 삭제할 권한이 없습니다.");
            }
        });
    }

    private void validateStatus(Reservation reservation) {
        if (reservation.isReserved()) {
            throw new InvalidReservationException("예약은 삭제할 수 없습니다. 관리자에게 문의해주세요.");
        }
    }
}
