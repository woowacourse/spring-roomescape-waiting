package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.theme.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    int deleteById(long id);

    Theme save(Theme theme);

    boolean existsByName(String name);

    // Todo : 인기 테마 조회
}
