package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.dto.response.FindWaitingRankResponse;
import roomescape.reservation.model.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    boolean existsByDateAndReservationTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndReservationTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId,
                                                                  Long memberId);

    @Query("""
                SELECT new roomescape.member.dto.response.FindWaitingRankResponse(
                    myWaiting.id AS waitingId,
                    myWaiting.theme.name AS theme,
                    myWaiting.date AS date,
                    myWaiting.reservationTime.startAt AS time,
                    COUNT(otherWaiting.id) AS waitingNumber
                )
                FROM Waiting otherWaiting
                JOIN Waiting myWaiting ON otherWaiting.date = myWaiting.date AND otherWaiting.reservationTime = myWaiting.reservationTime AND otherWaiting.theme = myWaiting.theme
                WHERE myWaiting.member.id = :memberId AND otherWaiting.id <= myWaiting.id
                GROUP BY otherWaiting.date, otherWaiting.reservationTime, otherWaiting.theme
            """)
    List<FindWaitingRankResponse> findAllWaitingResponses(Long memberId);
}
