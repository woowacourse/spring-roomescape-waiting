package roomescape.domain.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.Time;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public interface TimeQueryRepository extends Repository<Time, Long> {

    Optional<Time> findById(Long id);

    List<Time> findAll();

    boolean existsByStartAt(LocalTime time);

    default Time getById(Long id) {
        return findById(id).orElseThrow(
                () -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_TIME,
                        String.format("존재하지 않는 예약 시간입니다. 입력한 예약 시간 id:%d", id)));
    }
}
