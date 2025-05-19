package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;

@Repository
public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long>, ReservationTimeRepository {

    @Override
    Optional<ReservationTime> findById(Long id);

    @Override
    List<ReservationTime> findAll();

    @Override
    ReservationTime save(ReservationTime reservationTime);

    @Override
    void deleteById(Long id);

    @Override
    boolean existsByStartAt(LocalTime startAt);
}
