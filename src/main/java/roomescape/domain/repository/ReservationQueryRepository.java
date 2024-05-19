package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.dto.AvailableTimeDto;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public interface ReservationQueryRepository extends Repository<Reservation, Long> {

    Optional<Reservation> findById(Long id);

    @Query("""
            select r from Reservation r
            join fetch r.member member
            join fetch r.theme theme
            join fetch r.time time
            """)
    List<Reservation> findAll();

    @Query("""
            select new roomescape.domain.dto.AvailableTimeDto(
            rt.id, rt.startAt, (
                select count(r) > 0
                from Reservation r
                where r.time.id = rt.id
                and r.date = :date
                and r.theme.id = :themeId)
            )
            from Time rt
            """)
    List<AvailableTimeDto> findAvailableReservationTimes(LocalDate date, Long themeId);

    @Query("""
            select t
            from Theme t
            join Reservation r
            on r.theme.id = t.id
            where r.date between :startDate and :endDate
            group by t.id, t.name, t.description, t.thumbnail
            order by count(r.id) desc
            """)
    List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate);


    @Query("""
            select r
            from Reservation r
            join fetch r.member m
            join fetch r.time t
            join fetch r.theme th
            where (:memberId is null or r.member.id = :memberId)
            and (:dateFrom is null or r.date >= :dateFrom)
            and (:dateTo is null or r.date <= :dateTo)
            and (:themeId is null or r.theme.id = :themeId)
            """)
    List<Reservation> findByCriteria(Long memberId, LocalDate dateFrom, LocalDate dateTo, Long themeId);

    Optional<Reservation> findByDateAndTimeAndTheme(LocalDate date, Time time, Theme theme);

    boolean existsByTime(Time time);

    boolean existsByTheme(Theme theme);

    boolean existsByDateAndTimeAndTheme(LocalDate date, Time time, Theme theme);

    boolean existsByMemberAndDateAndTime(Member member, LocalDate date, Time time);

    List<Reservation> findAllByMemberIdOrderByDateDesc(Long memberId);

    default Reservation getById(Long id) {
        return findById(id).orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_RESERVATION,
                String.format("존재하지 않는 예약입니다. 요청 예약 id:%d", id)));
    }

    default Reservation getByDateAndTimeAndTheme(LocalDate date, Time time, Theme theme) {
        return findByDateAndTimeAndTheme(date, time, theme).orElseThrow(
                () -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_RESERVATION));
    }
}
