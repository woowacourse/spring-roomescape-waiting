package roomescape.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.ReservationTime;
import roomescape.dto.response.ReservationTimeWithIsBookedGetResponse;

public interface ReservationTimeDao {

    ReservationTime add(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    ReservationTime findById(Long id);

    int deleteById(Long id);

    boolean existByStartAt(LocalTime startAt);
}
