package roomescape.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.service.dto.MemberReservation;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    List<Reservation> findByThemeAndMemberAndDateBetween(Theme theme,
                                                         Member member,
                                                         LocalDate dateFrom,
                                                         LocalDate dateTo);

    void deleteById(long id);

    long countById(long id);

    long countByTime(ReservationTime time);

    long countByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    long countByDateAndTimeAndThemeAndMember(LocalDate date,
                                             ReservationTime time,
                                             Theme theme,
                                             Member member);

    List<Reservation> findAllByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findAllByMember(Member member);

    @Query("""
            SELECT new roomescape.service.dto.MemberReservation(
                       r.id,
                       r.theme.name,
                       r.date,
                       r.time.startAt,
                       r.createdAt,
                       (SELECT COUNT(subR)
                        FROM Reservation subR
                        WHERE subR.theme.id = r.theme.id
                          AND subR.date = r.date
                          AND subR.time.id = r.time.id
                          AND subR.createdAt < r.createdAt
                       )
                   )
            FROM Reservation r
            JOIN r.member m
            WHERE m.id = :memberId
            """)
    List<MemberReservation> findMemberReservation(@Param("memberId") Long memberId);

}
