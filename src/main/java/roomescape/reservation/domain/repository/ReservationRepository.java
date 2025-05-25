package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    boolean existsByReservationSlot(ReservationSlot reservationSlot);

    Reservation getById(Long reservationId);

    Optional<Reservation> findByReservationSlot(ReservationSlot reservationSlot);

    List<Reservation> findAll();

    List<Reservation> findAllByThemeIdAndMemberIdAndDateRange(Long themeId, Long memberId, LocalDate dateFrom,
                                                              LocalDate dateTo);

    List<Reservation> findAllByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findAllByMember(Member member);
}
