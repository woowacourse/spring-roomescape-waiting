package roomescape.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.presentation.dto.response.AvailableReservationTimeResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime time);

    void deleteById(Long id);

    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime startAt);

    @Query("""
        SELECT new roomescape.presentation.dto.response.AvailableReservationTimeResponse(
            rt.id,
            rt.startAt,
            CASE WHEN r.id IS NOT NULL THEN true ELSE false END
        )
        FROM ReservationTime rt
        LEFT JOIN Reservation r
            ON r.time = rt AND r.date = :date AND r.theme = :theme
    """)
    List<AvailableReservationTimeResponse> findAllAvailableReservationTimes(LocalDate date, Theme theme);
}
