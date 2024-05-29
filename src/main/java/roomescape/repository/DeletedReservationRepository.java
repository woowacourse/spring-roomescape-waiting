package roomescape.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import roomescape.model.CancelReservation;

public interface DeletedReservationRepository extends CrudRepository<CancelReservation, Long> {

    List<CancelReservation> findAll();
}
