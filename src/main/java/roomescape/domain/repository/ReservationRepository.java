package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import roomescape.domain.Reservation;

public interface ReservationRepository extends Repository<Reservation, Long> {
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    @Query("SELECT r.time.id FROM Reservation r WHERE r.date = :date AND r.theme.id = :themeId")
    List<Long> findTimeIdByDateAndThemeId(@Param("date") LocalDate date, @Param("themeId") Long themeId);

    List<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    void delete(Reservation reservation);

    void deleteAll();
}
