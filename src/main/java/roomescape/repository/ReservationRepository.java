package roomescape.repository;

import roomescape.domain.Reservation;
import roomescape.service.dto.UserReservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll(int page, int size);

    Optional<Reservation> findById(long id);

    List<Reservation> findByName(String name, int page, int size);

    List<UserReservation> findUserReservations(String name, int page, int size);

    Optional<Reservation> findBySchedule(LocalDate date, long timeId, long themeId);

    Reservation save(Reservation reservation);

    boolean existsByTimeId(long id);

    void update(Reservation reservation);

    void delete(Reservation reservation);
}
