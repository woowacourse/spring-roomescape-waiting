package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus.Status;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationRepository extends Repository<Reservation, Long> {
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findByMemberId(Long memberId);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    Optional<Reservation> findByDateAndTimeAndThemeAndMember(LocalDate date, ReservationTime time, Theme theme,
                                                             Member member);

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.member.name = :memberName
            AND r.theme.name = :themeName
            AND r.status.status = :status
            AND r.date >= :start
            AND r.date <= :end
            """)
    List<Reservation> findByPeriodAndThemeAndMember(@Param("start") LocalDate start,
                                                    @Param("end") LocalDate end,
                                                    @Param("memberName") String memberName,
                                                    @Param("themeName") String themeName,
                                                    @Param("status") Status status);

    Optional<Reservation> findTopByDateAndThemeAndTimeOrderByStatusPriorityAsc(LocalDate date, Theme theme,
                                                                               ReservationTime time);

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.theme = :theme
            AND r.date = :date
            AND r.time = :time
            ORDER BY r.status.priority DESC 
            LIMIT 1
            """)
    Optional<Reservation> findHighestPriority(@Param("date") LocalDate date,
                                              @Param("theme") Theme theme,
                                              @Param("time") ReservationTime time);

    long countByDateAndThemeAndTimeAndStatusPriorityIsLessThan(LocalDate date, Theme theme, ReservationTime time,
                                                               long priority);

    void delete(Reservation reservation);

    void deleteAll();

    void deleteByMember(Member findMember);
}
