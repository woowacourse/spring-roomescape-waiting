package roomescape.business.model.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Id;

public interface ReservationRepository {

    void save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findAllWithFilter(Id themeId, Id memberId, LocalDate dateFrom, LocalDate dateTo);

    Optional<Reservation> findById(Id id);

    boolean existById(Id reservationId);

    boolean existByTimeId(Id timeId);

    boolean existByThemeId(Id themeId);

    boolean isDuplicateDateAndTimeAndTheme(LocalDate date, LocalTime time, Id themeId);

    void deleteById(Id reservationId);
}
