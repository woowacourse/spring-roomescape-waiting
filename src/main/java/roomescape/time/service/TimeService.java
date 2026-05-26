package roomescape.time.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import roomescape.reservation.domain.ReservationTime;

public interface TimeService {
    ReservationTime create(LocalDateTime startAt, LocalDateTime endAt);

    List<ReservationTime> findAll();

    List<ReservationTime> findByDate(LocalDate date);

    ReservationTime findById(Long id);

    void deleteById(Long id);
}
