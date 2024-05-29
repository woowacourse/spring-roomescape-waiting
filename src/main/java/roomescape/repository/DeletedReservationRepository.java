package roomescape.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import roomescape.model.DeletedReservation;

public interface DeletedReservationRepository extends CrudRepository<DeletedReservation, Long> {

    List<DeletedReservation> findAll();
}
