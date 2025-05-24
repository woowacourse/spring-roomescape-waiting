package roomescape.reservation.model.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.vo.ReservationStatus;
import roomescape.reservation.model.vo.Schedule;

public interface ReservationRepository {

    List<Reservation> getAllByStatuses(List<ReservationStatus> statuses);

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    Reservation getById(Long id);

    boolean existDuplicatedSchedule(Schedule schedule);

    boolean existsByThemeId(Long reservationThemeId);

    boolean existsByTimeId(Long reservationTimeId);

    List<Reservation> getSearchReservations(Long themeId, Long memberId, LocalDate from, LocalDate to);

    List<Reservation> findAllByMemberId(Long memberId);
}
