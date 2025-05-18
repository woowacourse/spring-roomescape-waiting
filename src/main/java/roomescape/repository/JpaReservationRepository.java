package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<List<Reservation>> findByMemberId(long memberId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
}
