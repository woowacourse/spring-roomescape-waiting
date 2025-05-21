package roomescape.reservation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.service.dto.ReservationWaitWithRankResponse;

@Repository
public interface JpaReservationWaitRepository extends JpaRepository<ReservationWait, Long>, ReservationWaitRepository {

    @Override
    boolean existsByInfoDateAndInfoTimeIdAndInfoThemeIdAndInfoMemberId(ReservationDate date, Long timeId, Long themeId,
                                                                       Long memberId);

    @Override
    @Query("SELECT new roomescape.reservation.service.dto.ReservationWaitWithRankResponse(" +
           "    w, " +
           "    (SELECT COUNT(w2) + 1 " +
           "     FROM ReservationWait w2 " +
           "     WHERE w2.info.theme = w.info.theme " +
           "       AND w2.info.date = w.info.date " +
           "       AND w2.info.time = w.info.time " +
           "       AND w2.id < w.id)) " +
           "FROM ReservationWait w " +
           "WHERE w.info.member.id = :memberId")
    List<ReservationWaitWithRankResponse> findWithRankByInfoMemberId(Long memberId);

}
