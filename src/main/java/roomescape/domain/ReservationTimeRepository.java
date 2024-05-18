package roomescape.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public interface ReservationTimeRepository extends Repository<ReservationTime, Long> {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    boolean existsByStartAt(LocalTime time);

    void deleteById(Long id);

    default ReservationTime getById(Long id) {
        return findById(id).orElseThrow(
                () -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_TIME,
                        String.format("존재하지 않는 예약 시간입니다. 입력한 예약 시간 id:%d", id)));
    }
}
