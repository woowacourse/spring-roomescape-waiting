package roomescape.reservation.dao.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.reservation.model.Theme;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JpaThemeDao extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    int countById(Long id);
}
