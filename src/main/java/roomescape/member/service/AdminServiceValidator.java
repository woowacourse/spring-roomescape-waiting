package roomescape.member.service;

import org.springframework.stereotype.Component;
import roomescape.reservation.model.Slot;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;

@Component
public class AdminServiceValidator {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public AdminServiceValidator(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public void validateReservationExists(Slot slot) {
        if (reservationRepository.existsBySlot(slot)) {
            throw new IllegalArgumentException("이미 예약이 존재하여 대기를 예약으로 변경할 수 없습니다.");
        }
    }

    public void validateFirstWaiting(Slot slot, Long id) {
        if (waitingRepository.existsBySlotAndIdLessThan(slot, id)) {
            throw new IllegalArgumentException(id + "번 예약 대기보다 앞선 대기가 존재하여 예약으로 변경할 수 없습니다.");
        }
    }
}
