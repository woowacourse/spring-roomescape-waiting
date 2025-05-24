package roomescape.reservation.waiting.application.service;

import org.springframework.stereotype.Service;
import roomescape.common.exception.BusinessException;
import roomescape.reservation.waiting.domain.WaitingReservation;
import roomescape.reservation.waiting.domain.WaitingReservationRepository;

@Service
public class WaitingReservationCommandService {

    private final WaitingReservationRepository waitingReservationRepository;

    public WaitingReservationCommandService(WaitingReservationRepository waitingReservationRepository) {
        this.waitingReservationRepository = waitingReservationRepository;
    }

    public WaitingReservation save(final WaitingReservation waitingReservation) {
        return waitingReservationRepository.save(waitingReservation);
    }

    public void deleteByIdAndMemberId(final Long reservationId, final Long memberId) {
        validateExistsWaitingReservation(reservationId, memberId);

        waitingReservationRepository.deleteByIdAndMemberId(reservationId, memberId);
    }

    private void validateExistsWaitingReservation(Long reservationId, Long memberId) {
        if (!waitingReservationRepository.existsByIdAndMemberId(reservationId, memberId)) {
            throw new BusinessException("존재하지 않는 대기 예약입니다.");
        }
    }
}
