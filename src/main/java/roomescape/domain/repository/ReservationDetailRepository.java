package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationDetail;

public interface ReservationDetailRepository extends Repository<ReservationDetail, Long> {
    List<ReservationDetail> findAll();

    Optional<ReservationDetail> findById(Long id);

    default List<Long> findTimeIdByDateAndThemeId(LocalDate date, Long themeId) {
        return findByDateAndThemeId(date, themeId).stream()
                .map(TimeIdProjection::getTimeId)
                .toList();
    }

    List<TimeIdProjection> findByDateAndThemeId(LocalDate date, Long themeId);

    List<ReservationDetail> findAllByMemberId(Long memberId);

    @Query(""" 
            select r from ReservationDetail r
            join fetch r.member m 
            inner join r.theme th 
            where r.date >= :start and r.date <= :end
            and m.name = :memberName
            and th.name = :themeName """)
    List<ReservationDetail> findByPeriodAndMemberAndTheme(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("memberName") String memberName,
            @Param("themeName") String themeName);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    ReservationDetail save(ReservationDetail reservationDetail);

    void delete(ReservationDetail reservationDetail);

    void deleteAll();
}
