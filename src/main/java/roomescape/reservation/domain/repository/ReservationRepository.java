package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByDateAndTheme_Id(LocalDate date, Long themeId);

    List<Reservation> findByTheme_IdAndMember_IdAndDateBetween(long themeId, long memberId, LocalDate start,
                                                               LocalDate end);

    boolean existsByTime_Id(Long timeId);

    boolean existsByTheme_Id(Long themeId);

    List<Reservation> findByMember_Id(Long memberId);
}
