package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMember_IdAndTheme_IdAndDateDateBetween(Long memberId, Long themeId,
                                                                   LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByDateDateAndTheme_Id(LocalDate date, Long themeId);

    boolean existsByDateDateAndTime_IdAndTheme_Id(LocalDate date, Long timeId, Long themeId);
}
