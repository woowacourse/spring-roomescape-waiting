package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    // TODO: 메서드명 너무 김
    List<Reservation> findReservationsByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, LocalDate from, LocalDate to);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
