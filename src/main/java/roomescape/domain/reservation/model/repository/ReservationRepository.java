package roomescape.domain.reservation.model.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.model.entity.Reservation;

public interface ReservationRepository {

    List<Reservation> getAll();

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    Reservation getById(Long id);

    void remove(Reservation reservation);

    boolean existDuplicatedDateTime(LocalDate date, Long timeId, Long themeId);

    boolean existsByThemeId(Long reservationThemeId);

    boolean existsByTimeId(Long reservationTimeId);

    List<Reservation> getSearchReservations(Long themeId, Long memberId, LocalDate from, LocalDate to);
}
