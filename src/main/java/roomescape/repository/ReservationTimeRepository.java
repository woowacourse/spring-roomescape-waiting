package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime localTime);

    // TODO: 쿼리문 사용 방법 확인
    @Query(value = """
            SELECT
                t.id as time_id,
                t.start_at as time_value
            FROM reservation as r
            INNER JOIN reservation_time as t ON r.time_id = t.id
            INNER JOIN theme as th ON r.theme_id = th.id where date = :date and theme_id = :themeId
            """, nativeQuery = true)
    List<ReservationTime> findByDateAndThemeId(final LocalDate date, final Long themeId);
}
