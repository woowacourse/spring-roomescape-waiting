package roomescape.reservation.service;

import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;
import roomescape.reservation.model.Slot;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;

@Component
public class WaitingServiceValidator {
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public WaitingServiceValidator(WaitingRepository waitingRepository,
                                   ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    public void checkBothWaitingAndReservationNotExist(Slot slot) {
        if (bothWaitingAndReservationNotExist(slot)) {
            throw new IllegalArgumentException(
                    slot.date() + " " + slot.reservationTime().getStartAt() + "의 " + slot.theme().getName()
                            + " 테마는 바로 예약 가능하여 대기가 불가능합니다.");
        }
    }

    private boolean bothWaitingAndReservationNotExist(Slot slot) {
        return !waitingRepository.existsBySlot(slot) && !reservationRepository.existsBySlot(slot);
    }

    public void checkMemberAlreadyHasReservation(Member member, Slot slot) {
        if (reservationRepository.existsBySlotAndMemberId(slot, member.getId())) {
            throw new IllegalArgumentException("이미 본인의 예약이 존재하여 대기를 생성할 수 없습니다.");
        }
    }

    public void checkMemberAlreadyHasWaiting(Member member, Slot slot) {
        if (waitingRepository.existsBySlotAndMemberId(slot, member.getId())) {
            throw new IllegalArgumentException("이미 본인의 대기가 존재하여 대기를 생성할 수 없습니다.");
        }
    }

}
