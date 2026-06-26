package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Session;
import roomescape.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByName(String name);

    boolean existsByNameAndSession(String name, Session session);

    boolean existsBySession(Session session);

    Waiting findFirstBySessionOrderByIdAsc(Session session);

    List<Waiting> findBySessionOrderByIdAsc(Session session);
}
