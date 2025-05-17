package roomescape.reservation.domain;

public interface ReservationTimeCommandRepository {

    Long save(ReservationTime reservationTime);

    void deleteById(Long id);
}
