package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;

public interface ReservationRepository extends Repository<Reservation, Long> {
    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    default List<Long> findTimeIdByDateAndThemeId(LocalDate date, Long themeId) {
        return findByDateAndThemeId(date, themeId).stream()
                .map(TimeIdProjection::getTimeId)
                .toList();
    }

    List<TimeIdProjection> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);

    @Query(""" 
            select r from Reservation r
            join fetch r.member m 
            inner join r.theme th 
            where r.date >= :start and r.date <= :end
            and m.name = :memberName
            and th.name = :themeName """)
    List<Reservation> findByPeriodAndMemberAndTheme(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("memberName") String memberName,
            @Param("themeName") String themeName);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Reservation save(Reservation reservation);

    void delete(Reservation reservation);

    void deleteAll();
}
