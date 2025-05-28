package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.infrastructure.dto.ReservationWithRank;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllWaitingReservations(LocalDateTime now);

    List<Reservation> findAll();

    List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate dateFrom,
                                                      LocalDate dateTo);

    boolean existsByTimeId(Long timeId);

    boolean hasReservedReservation(LocalDate date, Long timeId, Long themeId);

    boolean hasSameReservation(LocalDate date, Long timeId, Long memberId, Long themeId, ReservationStatus status);

    boolean existsByThemeId(Long themeId);

    List<ReservationWithRank> findReservationWithRankByMemberId(Long memberId);

    Optional<Reservation> findById(Long id);
}
