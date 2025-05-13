package roomescape.persistence.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.business.domain.Reservation;

public interface ReservationDao {

    Reservation insert(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findAllByFilter(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByDateBetween(String startDate, String endDate);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    boolean deleteById(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
