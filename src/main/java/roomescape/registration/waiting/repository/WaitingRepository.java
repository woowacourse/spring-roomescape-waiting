package roomescape.registration.waiting.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.registration.waiting.Waiting;

public interface WaitingRepository extends CrudRepository<Waiting, Long> {

    Waiting save(Waiting waiting);

    List<Waiting> findAll();
}
