package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    Optional<Reservation> findByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId);

    @Query("""
            SELECT r, r.member, r.theme, r.time FROM Reservation r
            WHERE r.member.email = :email
            """)
    List<Reservation> findAllByMemberEmail(String email);
}
