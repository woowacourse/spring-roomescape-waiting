package roomescape.waiting.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long>, WaitingRepository {

    @Override
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Waiting w SET w.priority = (w.priority - :amount) WHERE w.priority >= :from")
    void pullPriority(@Param(value = "from") long from, @Param(value = "amount") int amount);
}
