package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
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
            from ReservationTime rt
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
            join fetch r.theme t
            join fetch r.member m
            where t.id = :themeId
            and m.id = :memberId
            and :dateFrom <= r.date
            and r.date <= :dateTo
            """)
    List<Reservation> findByCriteria(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    long countByTimeId(Long timeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findAllByMemberIdOrderByDateDesc(Long memberId);

    default Reservation getById(Long id) {
        return findById(id).orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_RESERVATION,
                String.format("존재하지 않는 예약입니다. 요청 예약 id:%d", id)));
    }
}
