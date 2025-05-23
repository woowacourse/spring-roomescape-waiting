package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r "
            + "JOIN FETCH r.member "
            + "JOIN FETCH r.theme "
            + "JOIN FETCH r.time ")
    List<Reservation> findAll();

    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(long memberId, long themeId, LocalDate fromDate,
                                                                LocalDate toDate);

    List<Reservation> findByDateBetween(LocalDate from, LocalDate to);

    Optional<Reservation> findFirstByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId);

    boolean existsByTimeId(long id);

    boolean existsByThemeId(long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    List<Reservation> findAllByMemberIdOrderByDateDesc(long id);
}
