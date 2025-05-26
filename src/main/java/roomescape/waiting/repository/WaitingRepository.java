package roomescape.waiting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.dto.WaitingInfoDataResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    Optional<Waiting> findByIdAndMemberId(Long id, Long memberId);

    @Query("SELECT w FROM Waiting w JOIN FETCH w.reservationInformation.time JOIN FETCH w.reservationInformation.theme JOIN FETCH w.member")
    List<Waiting> findAll();

    @Query("""
    SELECT w
    FROM Waiting w
    WHERE
        w.reservationInformation.theme = :theme
        AND w.reservationInformation.time = :time
        AND w.reservationInformation.date = :date
    ORDER BY w.createdAt ASC
    LIMIT 1
    """)
    Waiting findFirstByReservationInfo(LocalDate date, ReservationTime time, Theme theme);

    @Query("""
    SELECT
        new roomescape.waiting.repository.dto.WaitingInfoDataResponse(
            w,
            (COUNT(w2) + 1)
        )
    FROM Waiting w
    LEFT JOIN Waiting w2
    ON
        w2.createdAt < w.createdAt
        AND w2.reservationInformation.date = w.reservationInformation.date
        AND w2.reservationInformation.time.id = w.reservationInformation.time.id
        AND w2.reservationInformation.theme.id = w.reservationInformation.theme.id
    WHERE w.member.id = :memberId
    GROUP BY w
    ORDER BY w.createdAt ASC
    """)
    List<WaitingInfoDataResponse> findAllWaitingInfoByMemberId(@Param("memberId") Long memberId);
}
