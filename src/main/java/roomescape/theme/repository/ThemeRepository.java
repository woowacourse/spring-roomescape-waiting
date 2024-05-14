package roomescape.theme.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ThemeRepository extends ListCrudRepository<Theme, Long> {

    @Query(value = """
            SELECT theme.id, theme.name, theme.description, theme.thumbnail, COUNT(reservation.theme_id) AS reservation_count
            FROM theme
            JOIN reservation
            ON reservation.theme_id = theme.id
            WHERE reservation.date >= :startDate AND reservation.date <= :endDate
            GROUP BY theme.id, theme.name, theme.description, theme.thumbnail
            ORDER BY reservation_count DESC
            LIMIT :count
             """, nativeQuery = true)
    List<Theme> findThemesSortedByCountOfReservation(LocalDate startDate, LocalDate endDate, int count);
}
