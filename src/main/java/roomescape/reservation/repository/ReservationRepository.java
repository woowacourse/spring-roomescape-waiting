package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.member LEFT JOIN FETCH r.theme LEFT JOIN FETCH r.time")
    List<Reservation> findAllFetchJoin();

    @Query("SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.member LEFT JOIN FETCH r.theme LEFT JOIN FETCH r.time WHERE r.status = :status")
    List<Reservation> findAllByStatusFetchJoin(@Param("status") ReservationStatus status);

    List<Reservation> findByDateBetween(LocalDate start, LocalDate end);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByDateAndThemeAndTimeOrderByCreatedAt(LocalDate date, Theme theme, ReservationTime time);

    List<Reservation> findByDateBetweenAndMemberIdAndThemeId(
            LocalDate start,
            LocalDate end,
            Long memberId,
            Long themeId
    );

    Optional<Reservation> findByDateAndTimeIdAndThemeIdAndStatus(LocalDate date, Long timeId, Long themeId, ReservationStatus status);

    List<Reservation> findByMemberId(Long id);

    Optional<Reservation> findByIdAndStatus(Long id, ReservationStatus status);

    Boolean existsByTimeId(Long timeId);

    Boolean existsByThemeId(Long themeId);
}
