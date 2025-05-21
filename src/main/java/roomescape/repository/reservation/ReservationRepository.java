package roomescape.repository.reservation;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeId(LocalDate date, Long timeId);

    @Query(value = "SELECT time.id FROM Reservation WHERE date = :date AND theme.id = :themeId")
    List<Long> findAllTimeIdByDateAndThemeId(LocalDate date, Long themeId);

    @Query(value = "SELECT theme.id FROM Reservation WHERE date BETWEEN :startDate AND :endDate GROUP BY theme ORDER BY COUNT(*) DESC LIMIT 10")
    List<Long> findTopThemesByReservationCountBetween(LocalDate startDate, LocalDate endDate);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                         LocalDate dateTo);

    List<Reservation> findAllByMember(Member member);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, @NotNull Long timeId, @NotNull Long themeId,
                                                       Long memberId);
}
