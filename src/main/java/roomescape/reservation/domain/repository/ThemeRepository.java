package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
}
