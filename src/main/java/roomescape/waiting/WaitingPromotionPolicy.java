package roomescape.waiting;

import org.springframework.stereotype.Component;
import roomescape.reservation.Reservation;
import roomescape.slot.Slot;

@Component
public class WaitingPromotionPolicy {

    public Reservation promote(Waiting waiting, Slot slot) {
        if (!waiting.isFor(slot)) {
            throw new IllegalArgumentException("대기 슬롯과 예약 슬롯이 일치하지 않습니다.");
        }
        return Reservation.create(waiting.getMemberId(), slot);
    }
}
