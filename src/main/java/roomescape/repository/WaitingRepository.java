package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Waiting;
import roomescape.dto.response.WaitingWithRankDto;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findAllByMemberId(Long id);

    @Query("SELECT new roomescape.dto.response.WaitingWithRankDto(" +
           "    w, " +
           "    (SELECT COUNT(w2) * 1L + 1" +
           "     FROM Waiting w2 " +
           "     WHERE w2.theme = w.theme " +
           "       AND w2.date = w.date " +
           "       AND w2.reservationTime = w.reservationTime " +
           "       AND w2.createdAt < w.createdAt)) " +
           "FROM Waiting w " +
           "WHERE w.member.id = :memberId")
    List<WaitingWithRankDto> findWaitingsWithRankByMemberId(Long memberId);

    boolean existsByDateAndThemeIdAndReservationTimeIdAndMemberId(
            LocalDate date,
            Long themeId,
            Long reservationTimeId,
            Long memberId);
}
