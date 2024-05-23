package roomescape.theme.repository;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.model.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    default Theme getById(Long id) {
        return findById(id).orElseThrow(
                () -> new NoSuchElementException("식별자 " + id + "에 해당하는 테마가 존재하지 않습니다."));
    }

    @Query(value = """
            select t
            from Theme as t
            left join Reservation r on r.theme.id = t.id
            group by t.id
            order by count(t.id) desc
            """)
    List<Theme> findAllOrderByReservationCount(Pageable pageable);
}
