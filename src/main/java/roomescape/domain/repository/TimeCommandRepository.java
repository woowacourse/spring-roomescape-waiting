package roomescape.domain.repository;

import org.springframework.data.repository.Repository;
import roomescape.domain.Time;

public interface TimeCommandRepository extends Repository<Time, Long> {

    Time save(Time time);

    void delete(Time time);
}
