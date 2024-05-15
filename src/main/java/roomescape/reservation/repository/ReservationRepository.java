package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    List<Long> findTimeIdsByDateAndThemeId(LocalDate date, long themeId);

    List<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    List<Reservation> findByTimeId(long timeId);

    List<Reservation> findByThemeId(long themeId);

    List<Reservation> findByMemberId(long memberId);

    List<Reservation> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Modifying
    @Query("delete from Reservation where id = :id")
    int deleteById(@Param("id") long id);
}
