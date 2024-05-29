package roomescape.repository;

import java.time.LocalTime;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.entity.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);

}
