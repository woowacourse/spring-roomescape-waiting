package roomescape.infrastructure.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

public interface ThemeJpaRepository extends ThemeRepository, ListCrudRepository<Theme, Long> {

    @Override
    default List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate, int limitCount) {
        return findPopularThemesDateBetween(startDate, endDate, limitCount, BookStatus.BOOKED);
    }

    @Query("""
            select t from Theme as t
            left join Reservation as r on t.id = r.theme.id
            where r.date between :startDate and :endDate
            and r.status = :status
            group by t.id
            order by count(t.id) desc
            limit :limitCount
            """)
    List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate, int limitCount, BookStatus status);

    @Override
    default Theme getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 테마입니다."));
    }
}
