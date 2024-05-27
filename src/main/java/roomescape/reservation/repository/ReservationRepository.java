package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.WaitingResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.member LEFT JOIN FETCH r.theme LEFT JOIN FETCH r.time WHERE r.status = :status")
    List<Reservation> findAllByStatusFetchJoin(@Param("status") ReservationStatus status);

    List<Reservation> findByDateBetween(LocalDate start, LocalDate end);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByDateBetweenAndMemberIdAndThemeId(
            LocalDate start,
            LocalDate end,
            Long memberId,
            Long themeId
    );

    Optional<Reservation> findByDateAndTimeIdAndThemeIdAndStatus(LocalDate date, Long timeId, Long themeId, ReservationStatus status);

    Optional<Reservation> findByIdAndStatus(Long id, ReservationStatus status);

    Boolean existsByTimeId(Long timeId);

    Boolean existsByThemeId(Long themeId);

    @Query("SELECT new roomescape.reservation.dto.WaitingResponse(r , " +
            "(SELECT count(r1.id) FROM Reservation r1 " +
            "WHERE r.createdAt <= r1.createdAt " +
            "AND r.time = r1.time " +
            "AND r.theme = r1.theme " +
            "AND r.status = 'WAITING'))" +
            "FROM Reservation r ")
    List<WaitingResponse> getWaitingResponses();

    @Query("SELECT new roomescape.reservation.dto.MemberReservationResponse(r , " +
            "(SELECT count(r1.id) FROM Reservation r1 " +
            "WHERE r.createdAt <= r1.createdAt " +
            "AND r.time = r1.time " +
            "AND r.theme = r1.theme))" +
            "FROM Reservation r " +
            "WHERE r.member.id = :memberId ")
    List<MemberReservationResponse> getMemberReservationResponses(@Param("memberId") Long memberId);

    @Query("SELECT count(r.id) " +
            "FROM Reservation r " +
            "WHERE r.date = :#{#reservation.date} " +
            "AND r.time = :#{#reservation.time} " +
            "AND r.theme = :#{#reservation.theme} " +
            "AND r.createdAt <= :#{#reservation.createdAt} ")
    Long findSequence(@Param("reservation") Reservation reservation);
}
