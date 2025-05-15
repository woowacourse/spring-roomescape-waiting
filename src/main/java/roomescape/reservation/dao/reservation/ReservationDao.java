package roomescape.reservation.dao.reservation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.Theme;

public interface ReservationDao {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate);

    int deleteById(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
