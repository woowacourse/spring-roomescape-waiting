package roomescape.repository;

import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    default Theme getById(Long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 테마 입니다."));
    }

    boolean existsByName(ThemeName name);
}
