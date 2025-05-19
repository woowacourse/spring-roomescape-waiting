package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.exception.NotFoundException;

public interface ThemeRepository extends ListCrudRepository<Theme, Long> {

    @Query(""" 
            SELECT t.id, t.name, t.description, t.thumbnail
                               FROM Theme t
                               JOIN Reservation r
                               WHERE r.dateTime.date BETWEEN :startDate AND :endDate
                               GROUP BY t.id, t.name, t.description, t.thumbnail
                               ORDER BY COUNT(r.id) DESC
            """)
    List<Theme> findRankingByPeriod(LocalDate startDate, LocalDate endDate, int limit);

    default Theme getById(final long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다. id : " + id));
    }
}
