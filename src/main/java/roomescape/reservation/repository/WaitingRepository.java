package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.dto.response.FindWaitingResponse;
import roomescape.reservation.model.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    boolean existsByDateAndReservationTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndReservationTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);

    List<Waiting> findAllByMemberId(Long memberId);

    @Query("""
                SELECT new roomescape.member.dto.response.FindWaitingResponse(
                    myWaiting.id AS waitingId,
                    w.theme.name AS theme,
                    w.date AS date,
                    w.reservationTime.startAt AS time,
                    COUNT(1) AS waitingNumber
                )
                FROM Waiting w
                JOIN Waiting myWaiting ON w.date = myWaiting.date AND w.reservationTime = myWaiting.reservationTime AND w.theme = myWaiting.theme
                WHERE myWaiting.member.id = :memberId AND w.id <= myWaiting.id
                GROUP BY w.date, w.reservationTime, w.theme
            """)
    List<FindWaitingResponse> findAllWaitingResponses(Long memberId);
}
