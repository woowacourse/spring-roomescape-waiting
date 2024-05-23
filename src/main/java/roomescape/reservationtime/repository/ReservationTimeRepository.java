package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservationtime.model.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    default ReservationTime getById(Long id) {
        return findById(id).orElseThrow(
                () -> new NoSuchElementException("식별자 " + id + "에 해당하는 시간이 존재하지 않습니다."));
    }

    boolean existsByStartAt(LocalTime time);
}
