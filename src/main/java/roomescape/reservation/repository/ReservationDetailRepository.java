package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import roomescape.reservation.domain.ReservationDetail;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {
    int countReservationsByTime_Id(Long timeId);

    @Query("""
            SELECT r.id
            FROM ReservationDetail r
            WHERE r.date = :date AND r.theme.id = :themeId AND r.time.id = :timeId
            """)
    Optional<Long> findIdByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId);
}
