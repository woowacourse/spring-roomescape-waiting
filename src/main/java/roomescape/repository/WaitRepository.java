package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Wait;

public interface WaitRepository extends JpaRepository<Wait, Long> {

    @Query("SELECT w FROM Wait w")
    List<Wait> findAllWaits();

    @Query(
            value = """
                    SELECT w FROM Wait w 
                    WHERE w.slot.reservationDate = :reservationDate 
                      AND w.slot.time.id = :timeId 
                      AND w.slot.theme.id = :themeId
                    """)
    List<Wait> findBySlot(
            @Param("reservationDate") LocalDate reservationDate,
            @Param("timeId") Long timeId,
            @Param("themeId") Long themeId
    );

    List<Wait> findByName(String name);

    @Query(
            value = """
                    WITH slot_waiting_list AS (
                        SELECT `id`, ROW_NUMBER() OVER (ORDER BY created_at, id) AS `order`
                        FROM wait
                        WHERE `reservation_date` = :reservationDate AND `time_id` = :timeId AND `theme_id` = :themeId
                    )
                    SELECT `order` FROM slot_waiting_list WHERE `id` = :waitId
                    """,
            nativeQuery = true
    )
    Long calculateWaitingOrder(
            @Param("reservationDate") LocalDate reservationDate,
            @Param("timeId") Long timeId,
            @Param("themeId") Long themeId,
            @Param("waitId") Long waitId);

    boolean existsBySlot_Time_Id(Long timeId);

    boolean existsBySlot_Theme_Id(Long themeId);
}
