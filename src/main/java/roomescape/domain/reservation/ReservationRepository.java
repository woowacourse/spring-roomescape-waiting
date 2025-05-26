package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import roomescape.domain.member.Member;
import roomescape.domain.reservationitem.ReservationItem;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    List<Reservation> findAllReservations();

    Reservation save(final Reservation reservation);

    void deleteById(final long id);

    List<Reservation> findByMemberIdAndThemeIdAndDateFromAndDateTo(final Long memberId,
                                                                   final Long themeId,
                                                                   final LocalDate dateFrom,
                                                                   final LocalDate dateTo);

    boolean existByDateAndTimeIdAndThemeId(final LocalDate date,
                                           final long timeId,
                                           final long themeId);

    Optional<Reservation> findFirstByReservationItemAndReservationStatusOrderByIdAsc(ReservationItem reservationItem,
                                                                                     ReservationStatus reservationStatus);

    long countByReservationItemIdAndIdLessThan(
            @Param("reservationItemId") Long reservationItemId,
            @Param("currentReservationId") Long currentReservationId
    );

    boolean existsByMemberAndReservationItem(Member member, ReservationItem reservationItem);

    List<Reservation> findByReservationStatusOrderByIdDesc(ReservationStatus reservationStatus);

    List<Reservation> findByMemberId(Long memberId);
}
