package roomescape.repository;

import roomescape.domain.Reservation;
import roomescape.domain.Schedule;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll(int page, int size);

    Optional<Reservation> findById(long id);

    List<Reservation> findUserReservations(String name, int page, int size);

    Optional<Reservation> findBySchedule(Schedule schedule);

    Optional<String> findReserverNameByScheduleForUpdate(Schedule schedule);

    Reservation save(Reservation reservation);

    boolean existsByTimeId(long id);

    void update(Reservation reservation);

    void confirm(long id);

    boolean delete(Reservation reservation);

    boolean deletePendingById(long id);
}
