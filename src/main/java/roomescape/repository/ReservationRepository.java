package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import roomescape.model.Reservation;
import roomescape.model.ReservationInfo;
import roomescape.model.ReservationTime;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByReservationInfo(ReservationInfo reservationInfo);

    @Transactional
    @Query(value = """
                select t
                from Reservation r join ReservationTime t on r.reservationInfo.time.id = t.id
                where r.reservationInfo.date = ?1 and r.reservationInfo.theme.id = ?2
                """)
    List<ReservationTime> findReservationTimeByDateAndThemeId(LocalDate date, long themeId);

    @Transactional
    @Query(value = """
                select r
                from Reservation r
                where r.member.id = ?1 and r.reservationInfo.theme.id = ?2 and r.reservationInfo.date between ?3 and ?4
                """)
    List<Reservation> findByMemberIdAndThemeIdAndDate(long memberId, long themeId, LocalDate from, LocalDate to);

    List<Reservation> findByMemberId(long memberId);

    boolean existsByReservationInfo_TimeId(long timeId);

    boolean existsByReservationInfo_ThemeId(long themeId);

    Optional<Reservation> findByReservationInfo(ReservationInfo reservationInfo);
}
