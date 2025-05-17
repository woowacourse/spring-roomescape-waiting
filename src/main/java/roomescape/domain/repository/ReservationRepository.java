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

    boolean existsByThemeId(Long themeId);

    List<Reservation> findReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    int countBeforeWaitings(LocalDate date, Long themeId, Long timeId, Long reservationId);

    List<Reservation> findWaitingsReservation();
}
