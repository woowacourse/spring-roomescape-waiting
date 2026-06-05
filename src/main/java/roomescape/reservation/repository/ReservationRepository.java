package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Reservation> findByName(String name);

    List<Reservation> findAllWaitingBy(Long timeId, Long themeId);

    List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    boolean deleteById(Long id);

    boolean existsByTimeId(Long timeId);

    boolean isDuplicated(Long themeId, ReservationTime time);

    boolean isDuplicatedWithName(String name, Long themeId, ReservationTime time);

}
