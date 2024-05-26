package roomescape.reservation.service;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;
import roomescape.reservation.model.Slot;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;

@Component
public class ReservationServiceValidator {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationServiceValidator(ReservationRepository reservationRepository,
                                       WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public void validateExistReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NoSuchElementException("식별자 " + id + "에 해당하는 예약이 존재하지 않습니다. 삭제가 불가능합니다.");
        }
    }

    public void checkAlreadyExistReservation(Slot slot) {
        if (reservationRepository.existsBySlot(slot)) {
            throw new IllegalArgumentException("이미 예약이 존재하여 예약을 생성할 수 없습니다.");
        }
    }

    public void checkWaitingExists(Slot slot) {
        if (waitingRepository.existsBySlot(slot)) {
            throw new IllegalArgumentException("대기자가 있어 예약을 생성할 수 없습니다.");
        }
    }
}
