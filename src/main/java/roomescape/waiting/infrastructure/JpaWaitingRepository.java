package roomescape.waiting.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.waiting.domain.Waiting;

public interface JpaWaitingRepository extends ListCrudRepository<Waiting, Long> {

    @Query("""
            SELECT w 
            FROM Waiting w 
            JOIN FETCH w.reservation r            
            JOIN FETCH r.time t 
            JOIN FETCH r.theme th                   
            WHERE w.member.id = :memberId
            """)
    List<Waiting> findByWaitingsMemberId(Long memberId);
}
