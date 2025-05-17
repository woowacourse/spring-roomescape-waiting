package roomescape.reservation.domain;

public interface ReservationTimeCommandRepository {

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);
}
