package roomescape.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationCustomRepository {

    @Query("""
    SELECT r
    FROM Reservation r
    WHERE r.member.id = :memberId
    """)
    List<Reservation> findByMemberId(Long memberId);

    boolean existsByTimeId(Long reservationTimeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate reservationDate, Long timeId, Long themeId);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findByThemeIdAndDate(Long themeId, LocalDate reservationDate);
}
