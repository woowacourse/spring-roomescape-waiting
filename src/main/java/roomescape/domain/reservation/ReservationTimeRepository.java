package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface ReservationTimeRepository {

    ReservationTime create(ReservationTime reservationTime);

    Optional<ReservationTime> findById(long id);

    List<ReservationTime> findAll();

    void deleteById(long id);

    boolean existsByStartAt(LocalTime time);

    List<TimeSlot> getReservationTimeAvailabilities(LocalDate date, long themeId);

    default ReservationTime getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약 시간입니다."));
    }
}
