package roomescape.theme.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends CrudRepository<Theme, Long> {

    boolean existsByName(String name);

    List<Theme> findAll();

    // TODO : 쿼리구현
    @Query("SELECT t FROM Theme t")
    List<Theme> findTop10ThemesByReservationCountWithin7Days(int days, int limit);
}
