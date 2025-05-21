package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;

public interface ReservationRepository extends Repository<Reservation, Long>, ReservationRepositoryCustom {
    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByThemeId(Long themeId);

    Optional<Reservation> findByDateAndTimeSlotAndTheme(LocalDate date, TimeSlot time, Theme theme);

    List<Reservation> findByDateBetween(LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findByTimeSlotId(Long reservationTimeId);

    List<Reservation> findByMemberId(Long memberId);

    boolean existsByDateAndMemberAndThemeAndTimeSlot(LocalDate date, Member member, Theme theme, TimeSlot timeSlot);

    boolean existsByDateAndThemeAndTimeSlot(LocalDate date, Theme theme, TimeSlot timeSlot);

    void delete(Reservation reservation);
}

