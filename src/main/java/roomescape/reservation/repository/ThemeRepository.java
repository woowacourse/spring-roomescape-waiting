package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.model.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
}
