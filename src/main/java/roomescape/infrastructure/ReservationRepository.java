package roomescape.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);

    @Query("""
                        SELECT 
                            r.id AS reservation_id, 
                            m.id AS member_id,
                            m.name AS member_name, 
                            m.email AS member_email,
                            m.password AS member_password,
                            m.role AS member_role,
                            r.date AS reservation_date, 
                            t.id AS time_id, 
                            t.startAt AS time_value,
                            th.id AS theme_id,
                            th.name AS theme_name,
                            th.description AS theme_description,
                            th.thumbnail AS theme_thumbnail            
                        FROM Reservation AS r 
                        JOIN ReservationTime AS t ON r.time.id = t.id
                        JOIN Theme AS th ON r.theme.id = th.id
                        JOIN Member AS m ON r.member.id = m.id
                       WHERE m.id = :memberId AND th.id = :themeId AND r.date.date BETWEEN :from AND :to
            """)
    List<Reservation> findAllByMemberIdAndThemeIdInPeriod(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("from") String from,
            @Param("to") String to);

    boolean existsByTimeId(Long id);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    boolean existsByThemeId(Long id);
}
