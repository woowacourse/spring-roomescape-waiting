package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll(int page, int size);

    List<Reservation> findByGuestName(String guestName);

    Reservation save(Reservation reservation);

    boolean updateDateAndTime(Long id, LocalDate date, Long timeId);

    boolean cancelById(Long id, LocalDateTime now);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeIdAndThemeIdAndIdNot(LocalDate date, Long timeId, Long themeId, Long id);

    boolean existByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);
}
