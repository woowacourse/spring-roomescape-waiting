package roomescape.domain.reservation;

import roomescape.common.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime time);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(long id);

    List<ReservationTime> findByDateAndTheme(LocalDate date, long themeId);

    void delete(long id);

    boolean existsById(long id);

    default ReservationTime getById(long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다. 입력을 확인해 주세요."));
    }
}
