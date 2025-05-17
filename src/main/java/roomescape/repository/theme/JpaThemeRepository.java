package roomescape.repository.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;


@Repository
public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

}
