package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    int countByTimeId(Long timeId);

    int countByDateId(Long dateId);

    List<Reservation> findByThemeIdAndDateId(Long themeId, Long dateId);

    default List<Long> findReservedTimes(Long themeId, Long dateId) {
        return findByThemeIdAndDateId(themeId, dateId).stream()
                .map(reservation -> reservation.getTime().getId())
                .toList();
    }

    int countByThemeId(Long id);

    List<Reservation> findByName(String name);

    @Query("""
            select r
            from Reservation r
            where r.name = :name
              and (r.date.playDay > :currentDate
                or (r.date.playDay = :currentDate and r.time.startAt > :currentTime))
            order by r.date.playDay, r.time.startAt
            """)
    List<Reservation> findUpcomingByName(
            @Param("name") String name,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime
    );

    boolean existsByDateIdAndTimeIdAndThemeId(Long dateId, Long timeId, Long themeId);
}
