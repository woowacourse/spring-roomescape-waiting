package roomescape.domain.reservation;

import roomescape.exception.InvalidReservationException;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    Theme save(Theme theme);

    List<Theme> findAll();

    void deleteById(long id);

    boolean existsByName(String name);

    Optional<Theme> findById(long id);

    default Theme getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new InvalidReservationException("더이상 존재하지 않는 테마입니다."));
    }

    List<Theme> findByReservationTermAndCount(String startDate, String endDate, long count);
}
