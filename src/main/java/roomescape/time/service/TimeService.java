package roomescape.time.service;

import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TimeService {
    ReservationTime create(LocalDateTime startAt, LocalDateTime endAt);

    List<ReservationTime> findAll();

    List<ReservationTime> findByDate(LocalDate date);

    ReservationTime findById(Long id);

    void deleteById(Long id);

    void writeBehindInsert();
    void writeBehindUpdate();

    void explicitFlush();
}
