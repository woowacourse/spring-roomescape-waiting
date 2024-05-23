package roomescape.domain.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import roomescape.domain.ReservationWait;

public interface ReservationWaitRepository extends Repository<ReservationWait, Long> {

    ReservationWait save(ReservationWait wait);

    List<ReservationWait> findAll();

    void deleteAll();
}
