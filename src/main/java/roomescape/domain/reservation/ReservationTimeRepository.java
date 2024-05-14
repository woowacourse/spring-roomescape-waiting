package roomescape.domain.reservation;

import roomescape.exception.InvalidReservationException;

import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    void deleteById(long id);

    Optional<ReservationTime> findById(long id);

    default ReservationTime getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new InvalidReservationException("더이상 존재하지 않는 시간입니다."));
    }

    boolean existsByTime(String startAt);

    List<ReservationTime> findBookedTimesByDateAndTheme(String date, long themeId);
}
