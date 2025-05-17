package roomescape.reservation.domain;

public interface ReservationCommandRepository {

    Reservation save(Reservation reservation);

    void deleteById(Long id);
}
