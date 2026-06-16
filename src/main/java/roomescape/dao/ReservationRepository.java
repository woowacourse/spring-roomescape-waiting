package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByStatus(ReservationStatus status, Pageable pageable);

    long countByStatus(ReservationStatus status);

    boolean existsByTime_Id(Long timeId);

    boolean existsByTheme_Id(Long themeId);

    boolean existsByDateAndTime_IdAndTheme_IdAndStatus(
            LocalDate date, Long timeId, Long themeId, ReservationStatus status);

    boolean existsByDateAndTime_IdAndTheme_IdAndNameAndStatus(
            LocalDate date, Long timeId, Long themeId, String name, ReservationStatus status);

    List<Reservation> findByNameAndStatus(String name, ReservationStatus status);

    Optional<Reservation> findByIdAndStatus(Long id, ReservationStatus status);
}
