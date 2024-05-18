package roomescape.reservation.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Theme;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {


    Optional<Theme> findByThemeName(String name);
}
