package roomescape.domain;

public interface ReservationCommandRepository {

    Reservation create(Reservation reservation);

    void deleteById(long id);
}
