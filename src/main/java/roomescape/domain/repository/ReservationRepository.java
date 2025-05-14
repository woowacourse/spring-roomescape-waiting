package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    void deleteById(Long id);

    List<Reservation> findByThemeIdAndMemberIdAndDateGreaterThanEqualAndDateLessThanEqual(
            long themeId,
            long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    );
}
