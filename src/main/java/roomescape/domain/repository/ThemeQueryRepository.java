package roomescape.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.Theme;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public interface ThemeQueryRepository extends Repository<Theme, Long> {

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    default Theme getById(Long id) {
        return findById(id).orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_THEME,
                String.format("존재하지 않는 테마입니다. 요청 테마 id:%d", id)));
    }
}
