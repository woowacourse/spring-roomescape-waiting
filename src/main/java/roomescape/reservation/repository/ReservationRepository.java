package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByOrderByDetailDateAsc();

    List<Reservation> findAllByDetailTheme_IdAndDetailDate(Long themeId, LocalDate date);

    List<Reservation> findAllByMember_Id(Long memberId);

    List<Reservation> findAllByMember_IdOrderByDetailDateAsc(Long memberId);

    int countReservationsByDetailTime_Id(Long timeId);

    Optional<Reservation> findByDetail_Id(Long detailId);
}
