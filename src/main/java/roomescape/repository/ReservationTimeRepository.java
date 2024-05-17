package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import roomescape.model.ReservationTime;

public interface ReservationTimeRepository extends CrudRepository<ReservationTime, Long> {

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(long id);

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(long id);

    boolean existsById(long id);

    boolean existsByStartAt(LocalTime startAt);

    @Query("""
            SELECT r.time
            FROM Reservation r INNER JOIN ReservationTime t ON r.time.id = t.id
            WHERE r.date = :date AND r.theme.id = :themeId
            """)
    List<ReservationTime> findAllReservedTimes(@Param("date") LocalDate date, @Param("themeId") Long themeId);
}
