package roomescape.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

interface SpringDataJpaReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"member", "theme", "time"})
    Optional<Reservation> findById(Long id);

    @Query("""
               SELECT r
               FROM Reservation r
               JOIN FETCH r.member
               JOIN FETCH r.theme
               JOIN FETCH r.time
               WHERE
                   r.date = :date AND
                   r.time.id = :timeId AND
                   r.theme.id = :themeId
            """
    )
    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("""
              SELECT r.theme.id
              FROM Reservation r
              WHERE r.date BETWEEN :startDate AND :endDate
              GROUP BY r.theme.id
              ORDER BY count(*) DESC, r.theme.id ASC
            """
    )
    List<Long> findThemeReservationCountsForDate(LocalDate startDate, LocalDate endDate, Pageable limitTen);

    @EntityGraph(attributePaths = {"member", "theme", "time"})
    List<Reservation> findAll();

    @Query("SELECT r from Reservation r WHERE r.date = :date AND r.theme.id = :themeId")
    @EntityGraph(attributePaths = {"member", "theme", "time"})
    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    @EntityGraph(attributePaths = {"member", "theme", "time"})
    List<Reservation> findAllByMember(Member member);

    @Query("SELECT EXISTS (SELECT 1 FROM Reservation r WHERE r.time.id = :id)")
    boolean existsByTimeId(Long id);

    @Query("SELECT EXISTS (SELECT 1 FROM Reservation r WHERE r.theme.id = :id)")
    boolean existsByThemeId(Long id);

    @Query("""
            SELECT EXISTS
                (
                 SELECT 1
                 FROM Reservation r
                 WHERE
                     r.date = :date AND
                     r.time.id = :timeId AND
                     r.theme.id = :themeId
                 )
            """
    )
    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
