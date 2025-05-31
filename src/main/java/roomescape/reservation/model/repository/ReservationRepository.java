package roomescape.reservation.model.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.model.entity.Reservation;

public interface ReservationRepository {

    List<Reservation> getAll();

    Reservation save(Reservation reservation);

    void remove(Reservation reservation);

    boolean existDuplicatedDateTime(LocalDate date, Long timeId, Long themeId);

    boolean existsByThemeId(Long reservationThemeId);

    boolean existsByTimeId(Long reservationTimeId);

    List<Reservation> getSearchReservations(Long themeId, Long memberId, LocalDate from, LocalDate to);

    Optional<Reservation> findById(Long id);

    Reservation getById(Long id);

    List<Reservation> findAllByMemberId(Long memberId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
