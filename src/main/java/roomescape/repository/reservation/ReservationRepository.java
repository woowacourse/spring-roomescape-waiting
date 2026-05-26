package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll();

    List<Reservation> findAllByName(String name);

    Optional<Reservation> findById(long id);

    Optional<Reservation> findByIdAndName(long id, String name);

    void deleteById(long id);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    boolean existsByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId);

    boolean existsByDateAndThemeIdAndTimeIdExcludingId(LocalDate date, long themeId, long timeId, long reservationId);

    List<Long> findReservedTimeIdsByDateAndThemeId(LocalDate date, long themeId);

    Long findReservationIdByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);
}
