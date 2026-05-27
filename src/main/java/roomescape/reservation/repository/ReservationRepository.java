package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWaitingDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    Optional<ReservationWaitingDto> findWaitingById(Long id);

    List<Reservation> findAll(int page, int size);

    List<ReservationWaitingDto> findAllByGuestName(String guestName);

    List<ReservationWaitingDto> findAllByGuestNameExceptCanceled(String guestName);

    Reservation save(Reservation reservation);

    boolean updateDateAndTime(Long id, LocalDate date, Long timeId, Status status);

    boolean cancelById(Long id);

    boolean updateStatus(Long id, Status status);

    Optional<Long> findFirstWaitingIdBySlot(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeIdAndThemeIdAndGuestNameExceptCanceled(LocalDate date, Long timeId, Long themeId, String guestName);

    boolean existsReservationBySlot(LocalDate date, Long timeId, Long themeId);

    boolean existByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);
}
