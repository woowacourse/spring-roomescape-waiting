package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.entity.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll();

    List<Reservation> findByMemberId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Reservation save(Reservation reservation);

    void deleteById(Long id);

}
