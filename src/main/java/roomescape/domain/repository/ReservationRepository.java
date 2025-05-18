package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.service.result.WaitingWithRank;


public interface ReservationRepository {

    Reservation save(Reservation reservation);

    void deleteById(Long reservationId);

    Optional<Reservation> findById(Long reservationId);

    List<Reservation> findByMemberId(Long memberId);

    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    boolean existsByTimeId(Long reservationTimeId);

    boolean existsDuplicateReservation(LocalDate reservationDate, Long timeId, Long themeId);

    boolean isReservationSlotEmpty(LocalDate reservationDate, Long timeId, Long themeId);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findWaitingsReservation();

    Optional<Reservation> findFirstWaiting(LocalDate date, Long themeId, Long timeId);

    boolean hasAlreadyReservedOrWaited(Long memberId, Long themeId, Long timeId, LocalDate date);
}
