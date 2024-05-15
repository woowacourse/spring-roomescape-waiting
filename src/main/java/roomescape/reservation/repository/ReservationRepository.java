package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;

@Repository
public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {
    @Query(value = """
            SELECT reservation.id, reservation.member_id, reservation.date, reservation.time_id, reservation.theme_id,
                    member.name, member.email, member.role,
                    reservation_time.start_at,
                    theme.name AS theme_name, theme.description, theme.thumbnail
            FROM reservation
            JOIN member ON reservation.member_id = member.id
            JOIN reservation_time ON reservation.time_id = reservation_time.id
            JOIN theme ON reservation.theme_id = theme.id
            WHERE theme_id = NVL(:themeId, theme_id) 
                    AND member_id = NVL(:memberId, member_id) 
                    AND date BETWEEN NVL(:startDate, date) AND NVL(:endDate, date) 
            """, nativeQuery = true)
    List<Reservation> findAll(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate);

    List<Reservation> findByMember_id(Long memberId);
}
