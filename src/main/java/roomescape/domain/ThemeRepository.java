package roomescape.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public interface ThemeRepository extends Repository<Theme, Long> {

    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    void deleteById(Long id);

    default Theme getById(Long id) {
        return findById(id).orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_THEME,
                String.format("존재하지 않는 테마입니다. 요청 테마 id:%d", id)));
    }
}
