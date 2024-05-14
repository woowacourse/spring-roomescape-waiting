package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.theme.model.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    @Query(value = """
            select t
            from Theme as t
            left join Reservation r on r.theme.id = t.id
            group by t.id
            order by count(t.id) desc
            """)
    List<Theme> findAllOrderByReservationCount(Pageable pageable);

    boolean existsById(Long id);

    void deleteById(Long id);
}
