package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    default Reservation getById(Long id) {
        return findById(id).orElseThrow(
                () -> new NoSuchElementException("식별자 " + id + "에 해당하는 예약이 존재하지 않습니다."));
    }

    Optional<Reservation> findByDateAndReservationTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    default Reservation getByDateAndReservationTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return findByDateAndReservationTimeIdAndThemeId(date, timeId, themeId).orElseThrow(() ->
                new NoSuchElementException(date + "의 time: " + timeId + ", theme: " + themeId + "의 예약이 존재하지 않습니다."));
    }

    @Query("""
            select r, m, rt, t
            from Reservation r
            join fetch Member m
            on r.member.id = m.id
            join fetch ReservationTime rt
            on r.reservationTime.id = rt.id
            join fetch Theme t
            on r.theme.id = t.id
            """)
    List<Reservation> findAll();

    @Query("""
            select r, m, rt, t
            from Reservation r
            join fetch Member m
            on r.member.id = m.id
            join fetch ReservationTime rt
            on r.reservationTime.id = rt.id
            join fetch Theme t
            on r.theme.id = t.id
            where m.id = :memberId
            """)
    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    @Query("""
            select r, m, rt, t
            from Reservation r
            join fetch Member m
            on r.member.id = m.id
            join fetch ReservationTime rt
            on r.reservationTime.id = rt.id
            join fetch Theme t
            on r.theme.id = t.id
            where t.id = :themeId and m.id = :memberId and r.date between :dateFrom and :dateTo
            """)
    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                                LocalDate dateTo);

    boolean existsByDateAndReservationTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByReservationTimeId(Long reservationTimeId);

    boolean existsByThemeId(Long id);
}
