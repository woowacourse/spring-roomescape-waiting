package roomescape.adapter.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Waiting;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByDateAndTime_IdAndTheme_IdOrderByOrderIndexAsc(LocalDate date, Long timeId, Long themeId);

    List<Waiting> findByNameOrderByDateAscTime_StartAtAsc(String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Waiting w set w.orderIndex = :orderIndex where w.id = :id")
    void updateOrderIndex(@Param("id") Long id, @Param("orderIndex") int orderIndex);
}
