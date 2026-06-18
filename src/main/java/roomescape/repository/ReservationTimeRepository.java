package roomescape.repository;

import static roomescape.domain.exception.DomainErrorCode.RESERVATION_TIME_NOT_FOUND;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationTime;
import roomescape.domain.exception.RoomEscapeException;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    List<ReservationTime> findAllByOrderByStartAtAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rt from ReservationTime rt where rt.id = :id")
    Optional<ReservationTime> findByIdForUpdate(@Param("id") Long id);

    default ReservationTime getById(Long id, String message) {
        return findById(id).orElseThrow(() -> new RoomEscapeException(RESERVATION_TIME_NOT_FOUND, message));
    }

    default ReservationTime getByIdForUpdate(Long id, String message) {
        return findByIdForUpdate(id).orElseThrow(() -> new RoomEscapeException(RESERVATION_TIME_NOT_FOUND, message));
    }
}
