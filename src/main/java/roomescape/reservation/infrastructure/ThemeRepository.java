package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.response.ThemeResponse;

public interface ThemeRepository extends Repository<Theme, Long> {
    Theme save(Theme theme);

    List<Theme> findAll();

    void deleteById(long id);

    Optional<Theme> findByName(String name);

    Optional<Theme> findById(Long id);

    @Query("""
            SELECT new roomescape.reservation.dto.response.ThemeResponse(
               t.id, t.name, t.description, t.thumbnail
            )
            FROM Theme t
            LEFT JOIN Reservation r ON r.theme.id = t.id AND r.reservationTime.date BETWEEN :dateFrom AND :dateTo
            GROUP BY t.id, t.name
            ORDER BY COUNT(r) DESC, t.name ASC
            """)
    List<ThemeResponse> findByDateBetweenOrderByReservationCountDescNameAsc(LocalDate dateFrom, LocalDate dateTo,
                                                                            Pageable pageable);
}