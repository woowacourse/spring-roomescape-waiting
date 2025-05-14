package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {
    //TODO: findPopular 구현하기
    List<Theme> findPopular(LocalDate start, LocalDate end);
}
