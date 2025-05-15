package roomescape.reservation.dao.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.model.Theme;

@Repository
public interface JpaThemeDao extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    int countById(Long id);
}
