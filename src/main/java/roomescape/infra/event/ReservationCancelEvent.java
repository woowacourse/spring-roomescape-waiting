package roomescape.infra.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import roomescape.domain.reservation.Reservation;

@Getter
public class ReservationCancelEvent extends ApplicationEvent {
    private final Reservation reservation;

    public ReservationCancelEvent(Object source, Reservation reservation) {
        super(source);
        this.reservation = reservation;
    }
}
