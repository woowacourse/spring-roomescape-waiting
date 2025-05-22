package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.service.WaitingRepository;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long>, WaitingRepository {

    @Query("SELECT COUNT(w) > 0 FROM Waiting w " +
            "WHERE w.member.id = :memberId " +
            "AND w.reservation.theme.id = :themeId " +
            "AND w.reservation.reservationTime.id = :timeId " +
            "AND w.reservation.reservationDate.reservationDate = :date")
    boolean existsBySameReservation(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("timeId") Long reservationTimeId,
            @Param("date") LocalDate date
    );
    
    void deleteById(Long id);

    List<Waiting> findAll();
}
