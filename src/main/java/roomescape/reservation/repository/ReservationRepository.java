package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.WaitingRankReservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();
    List<Reservation> findAllByThemeAndDate(Theme theme, ReservationDate date);
    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, ReservationDate from, ReservationDate to);
    List<Reservation> findAllByReservationStatus(ReservationStatus reservationStatus);
    List<Reservation> findAllByDateAndReservationTimeAndThemeAndReservationStatusOrderByAsc(
            ReservationDate date,
            ReservationTime reservationTime,
            Theme theme,
            ReservationStatus reservationStatus
    );
    List<WaitingRankReservation> findAllWaitingRankByMember(Member member);

    void deleteById(Long id);

    boolean existsByReservationTime(ReservationTime reservationTime);
    boolean existsByDuplicateMember(
            ReservationDate date,
            ReservationTime reservationTime,
            Theme theme,
            Member member
    );
    boolean existsDuplicateStatus(
            ReservationTime reservationTime,
            ReservationDate date,
            Theme theme,
            ReservationStatus reservationStatus
    );
}
