package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Theme;
import roomescape.service.exception.ThemeNotFoundException;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    default Theme fetchById(long id) {
        return findById(id).orElseThrow(() -> new ThemeNotFoundException("존재하지 않는 테마입니다."));
    }
}
