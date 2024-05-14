package roomescape.domain;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query("select case when(count(*)>0) then true else false end from ReservationTime t where t.startAt = :startAt")
    boolean existByStartAt(LocalTime startAt);
}
