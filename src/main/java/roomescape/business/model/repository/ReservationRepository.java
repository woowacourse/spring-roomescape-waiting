package roomescape.business.model.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.presentation.dto.response.ReservationWithAhead;

public interface ReservationRepository {

    void save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findAllWithFilter(Id themeId, Id memberId, LocalDate dateFrom, LocalDate dateTo);

    List<ReservationWithAhead> findReservationsWithAhead(Id userId);

    Optional<Reservation> findById(Id id);

    boolean existById(Id reservationId);

    boolean existByTimeId(Id timeId);

    boolean existByThemeId(Id themeId);

    boolean isDuplicateDateAndTimeAndTheme(LocalDate date, LocalTime time, Id themeId);

    void deleteById(Id reservationId);

    List<Reservation> findAllWaitingReservations();

    void updateWaitingReservations(ReservationDate date, ReservationTime time, Theme theme);
}
