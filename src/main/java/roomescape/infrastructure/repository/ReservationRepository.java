package roomescape.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    Optional<Reservation> findById(Long id);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByTime(ReservationTime time);

    boolean existsByTheme(Theme theme);

    @Query("SELECT r FROM Reservation r WHERE r.theme =:theme AND r.member =:member AND (r.date BETWEEN :dateFrom AND :dateTo)")
    List<Reservation> findAllByThemeAndMemberAndDate(Theme theme, Member member, LocalDate dateFrom, LocalDate dateTo);
}
