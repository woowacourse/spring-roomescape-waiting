package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Waiting;
import roomescape.dto.projection.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("SELECT new roomescape.dto.projection.WaitingWithRank("
            + "w, "
            + "(SELECT COUNT(w2) FROM Waiting w2 "
            + " WHERE w2.theme = w.theme "
            + "   AND w2.date = w.date "
            + "   AND w2.time = w.time "
            + "   AND w2.id < w.id)) "
            + "FROM Waiting w "
            + "JOIN FETCH w.theme "
            + "JOIN FETCH w.time "
            + "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWithRankByMember_Id(@Param("memberId") Long memberId);

    boolean existsByMember_IdAndDateAndTime_IdAndTheme_Id(Long memberId, LocalDate date, Long timeId, Long themeId);

    Optional<Waiting> findFirstByDateAndTime_IdAndTheme_IdOrderById(LocalDate date, Long timeId, Long themeId);
}
