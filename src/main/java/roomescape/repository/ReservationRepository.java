package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    @Query(value = """
            select t
            from Reservation r join ReservationTime t on r.time.id = t.id
            where r.date = ?1 and r.theme.id = ?2
            """)
    List<ReservationTime> findReservationTimeBooked(LocalDate date, long themeId);

    @Query(value = """
            select r
            from Reservation r
            where r.member.id = ?1 and r.theme.id = ?2 and r.date between ?3 and ?4
            """)
    List<Reservation> findByMemberIdAndThemeIdAndDate(long memberId, long themeId, LocalDate from, LocalDate to);

    List<Reservation> findByMemberId(long memberId);

    List<Reservation> findByTimeIdAndThemeIdAndDate(long timeId, long themeId, LocalDate date);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, long timeId, long themeId, long memberId);
}
