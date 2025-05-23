package roomescape.infrastructure.db;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.model.Waiting;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByPendingReservation_MemberId(Long id);

    @Query("""
            SELECT COUNT(w) FROM Waiting w
            WHERE w.registeredAt < :registeredAt
            """)
    int countWaitingBefore(@Param("registeredAt") LocalDateTime registeredAt);

}

