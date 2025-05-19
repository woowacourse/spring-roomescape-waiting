package roomescape.waiting.infrastructure;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.waiting.domain.Waiting;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long> {
    @Query("""
            SELECT w FROM Waiting w
            JOIN FETCH w.member
            JOIN FETCH w.spec.theme
            JOIN FETCH w.spec.time
            """)
    Collection<Waiting> findAllWithEagerLoading();
}
