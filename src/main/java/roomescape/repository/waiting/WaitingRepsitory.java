package roomescape.repository.waiting;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;

@Repository
public interface WaitingRepsitory extends JpaRepository<Waiting, Long> {

    @Query("SELECT new roomescape.domain.WaitingWithRank(" +
            "    w, " +
            "    (SELECT COUNT(w2) " +
            "     FROM Waiting w2 " +
            "     WHERE w2.theme = w.theme " +
            "       AND w2.date = w.date " +
            "       AND w2.time = w.time " +
            "       AND w2.id < w.id)) " +
            "FROM Waiting w " +
            "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, @NotNull Long timeId, @NotNull Long themeId, Long memberId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Waiting findFirstWaitingByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
