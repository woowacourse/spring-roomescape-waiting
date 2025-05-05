package roomescape.reservation.domain;

public interface ReservationCommandRepository {

    Long save(Reservation reservation);

    void deleteById(Long id);
}
