package roomescape.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.exception.ResourceNotFoundCustomException;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    default Theme getThemeById(Long id) {
        return this.findById(id)
                .orElseThrow(() -> new ResourceNotFoundCustomException("아이디에 해당하는 테마를 찾을 수 없습니다."));
    }
}
