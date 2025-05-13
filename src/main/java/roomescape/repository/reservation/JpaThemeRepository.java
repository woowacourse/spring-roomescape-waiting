package roomescape.repository.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Theme;


@Repository
public interface JpaThemeRepository extends JpaRepository<Theme, Long>, ThemeRepository {

}
