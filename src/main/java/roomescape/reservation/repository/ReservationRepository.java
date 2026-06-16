package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.member.id = :memberId ORDER BY r.date DESC, r.time.startAt DESC")
    List<Reservation> findByMemberId(@Param("memberId") Long memberId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId);
}