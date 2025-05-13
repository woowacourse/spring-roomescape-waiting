package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    ReservationTime save(ReservationTime reservationTime); //TODO: 이미 구현되어있어서 나중에 삭제

    List<ReservationTime> findAll(); //TODO: 이미 구현되어있어서 나중에 삭제

    Optional<ReservationTime> findById(Long id); //TODO: 이미 구현되어있어서 나중에 삭제

    boolean existsByTime(LocalTime time); //TODO: 필요

    boolean existsReservationByTimeId(Long id); //TODO: 메서드명 수정 필요

    void deleteById(Long id); //TODO: 이미 구현되어있어서 나중에 삭제
}
