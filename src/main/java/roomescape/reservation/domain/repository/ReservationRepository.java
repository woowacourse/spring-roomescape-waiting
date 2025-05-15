package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByDateAndTheme_Id(LocalDate date, Long themeId);

    @Query("""
            SELECT r FROM Reservation r 
            WHERE (:themeId IS NULL OR r.theme.id = :themeId) 
            AND (:memberId IS NULL OR r.member.id = :memberId) 
            AND (:from IS NULL OR r.date >= :from) 
            AND (:to IS NULL OR r.date <= :to)
            """)
    List<Reservation> findByTheme_IdAndMember_IdAndDateBetween(
            Long themeId,
            Long memberId,
            LocalDate from,
            LocalDate to);

    boolean existsByTime_Id(Long timeId);

    boolean existsByTheme_Id(Long themeId);

    List<Reservation> findByMember_Id(Long memberId);
}
