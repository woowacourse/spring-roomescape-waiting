package roomescape.business.model.repository;

import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface Reservations {

    void save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findAllWithFilter(Id themeId, Id memberId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findAllByUserId(Id userId);

    Optional<Reservation> findById(Id id);

    boolean existById(Id reservationId);

    boolean existByTimeId(Id timeId);

    boolean existByThemeId(Id themeId);

    boolean isDuplicateDateAndTimeAndTheme(LocalDate date, LocalTime time, Id themeId);

    void deleteById(Id reservationId);
}
