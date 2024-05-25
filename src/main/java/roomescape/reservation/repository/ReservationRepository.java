package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByOrderByDetailDateAsc();

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.detail.theme.id = :themeId
            AND r.detail.date = :date
            """
    )
    List<Reservation> findAllByDetailTheme_IdAndDetailDate(Long themeId, LocalDate date);

    List<Reservation> findAllByMember_Id(Long memberId);

    List<Reservation> findAllByMember_IdOrderByDetailDateAsc(Long memberId);

    Optional<Reservation> findByDetail_IdAndMember_Id(Long detailId, Long memberId);

    Optional<Reservation> findByDetail_Id(Long detailId);
}
