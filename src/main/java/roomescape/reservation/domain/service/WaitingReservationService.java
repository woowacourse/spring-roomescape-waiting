package roomescape.reservation.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.domain.entity.MemberReservation;
import roomescape.reservation.domain.entity.Reservation;
import roomescape.reservation.domain.entity.ReservationStatus;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.repository.MemberReservationRepository;

import java.util.List;

@Service
public class WaitingReservationService {

    private final MemberReservationRepository memberReservationRepository;

    public WaitingReservationService(MemberReservationRepository memberReservationRepository) {
        this.memberReservationRepository = memberReservationRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> readWaitingReservations() {
        return memberReservationRepository.findByStatus(ReservationStatus.WAITING).stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmWaitingReservation(Long id) {
        MemberReservation memberReservation = findMemberReservationById(id);
        memberReservation.validateWaitingStatus();

        Reservation reservation = memberReservation.getReservation();
        validateConfirmReservationExists(reservation);
        validateRankCanConfirm(reservation, memberReservation);

        memberReservation.setStatus(ReservationStatus.CONFIRMATION);
    }

    private MemberReservation findMemberReservationById(Long id) {
        return memberReservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약입니다."));
    }

    private void validateConfirmReservationExists(Reservation reservation) {
        memberReservationRepository.findByReservationAndStatusIsConfirmation(reservation)
                .ifPresent((confirmReservation) -> {
                    throw new BadRequestException("이미 예약이 존재해 대기를 승인할 수 없습니다.");
                });
    }

    private void validateRankCanConfirm(Reservation reservation, MemberReservation memberReservation) {
        Long waitingRank = memberReservationRepository.countByReservationAndCreatedAtBefore(
                reservation, memberReservation.getCreatedAt()
        );
        memberReservation.validateRankConfirm(waitingRank);
    }
}
