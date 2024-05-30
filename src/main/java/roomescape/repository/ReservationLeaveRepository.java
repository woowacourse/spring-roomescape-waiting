package roomescape.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import roomescape.model.ReservationLeave;

public interface ReservationLeaveRepository extends CrudRepository<ReservationLeave, Long> {

    List<ReservationLeave> findAll();
}
