package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.entity.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(Long memberId);

    @Query("""
        SELECT DISTINCT r.time.id
        FROM Reservation r
        WHERE r.theme.id = :themeId
            AND r.date = :date
        """)
    List<Long> findBookedTimeIds(LocalDate date, Long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
