package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMember_IdAndTheme_IdAndDateValueBetween(Long memberId, Long themeId,
                                                                    LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByDateValueAndTheme_Id(LocalDate date, Long themeId);

    boolean existsByDateValueAndTime_IdAndTheme_Id(LocalDate date, Long timeId, Long themeId);
}
