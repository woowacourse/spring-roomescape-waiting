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

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.theme
        JOIN FETCH r.time
        WHERE r.theme.id =:themeId
        AND r.member.id =:memberId
        AND (r.date BETWEEN :dateFrom AND :dateTo)
        """)
    List<Reservation> findAllByThemeAndMemberAndDate(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findAllByMember(Member member);

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);
}
