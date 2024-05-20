package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface ThemeRepository extends ListCrudRepository<Theme, Long> {

    @Query("""
            select t from Theme as t
            left join Reservation as r on t.id = r.theme.id
            where r.date between :startDate and :endDate
            group by t.id
            order by count(t.id) desc
            limit :limitCount
            """)
    List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate, int limitCount);

    default Theme getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 테마입니다."));
    }
}
