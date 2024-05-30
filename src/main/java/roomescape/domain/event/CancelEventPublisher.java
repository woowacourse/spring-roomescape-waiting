package roomescape.domain.event;


import roomescape.domain.reservation.Reservation;

public interface CancelEventPublisher {

    void publishCancelEvent(Reservation reservation);

}
