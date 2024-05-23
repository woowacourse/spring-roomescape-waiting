package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.dto.ReservationReadOnly;
import roomescape.domain.reservation.slot.ReservationSlot;
import roomescape.domain.reservation.slot.ReservationTime;
import roomescape.domain.reservation.slot.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Reservation findBySlot(ReservationSlot slot);

    @Query("""
            select r
            from Reservation r
            join fetch r.slot.time
            join fetch r.slot.theme
            where r.member = :member and r.slot.date >= :date
            """)
    List<Reservation> findByMemberAndSlot_DateGreaterThanEqual(Member member, LocalDate date);

    @Query("""
            select r.slot.theme
            from Reservation r
            where r.slot.date between :startDate and :endDate
            group by r.slot.theme
            order by count(r) desc
            """)
    List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, Limit limit);

    @Query("""
            select new roomescape.domain.reservation.dto.ReservationReadOnly(
                r.id,
                r.member,
                r.slot.date,
                r.slot.time,
                r.slot.theme
            )
            from Reservation r
            where (:startDate is null or r.slot.date >= :startDate)
                and (:endDate is null or r.slot.date <= :endDate)
                and (:themeId is null or r.slot.theme.id = :themeId)
                and (:memberId is null or r.member.id = :memberId)""")
    List<ReservationReadOnly> findByConditions(@Nullable LocalDate startDate, @Nullable LocalDate endDate, @Nullable Long themeId,
                                               @Nullable Long memberId);

    @Query("select r.slot.time from Reservation r where r.slot.date = :date and r.slot.theme = :theme")
    List<ReservationTime> findTimesByDateAndTheme(LocalDate date, Theme theme);

    boolean existsBySlot_Time(ReservationTime time);

    boolean existsBySlot_Theme(Theme theme);
}
