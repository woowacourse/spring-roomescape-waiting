package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.NotFoundException;
import roomescape.infrastructure.db.ThemeJpaRepository;
import roomescape.model.Theme;

@Repository
@RequiredArgsConstructor
public class ThemeRepositoryImpl implements ThemeRepository {

    private final ThemeJpaRepository themeJpaRepository;

    @Override
    public boolean isDuplicatedThemeName(String name) {
        return themeJpaRepository.existsByName(name);
    }

    @Override
    public List<Theme> findPopularThemesInPeriod(LocalDate startDate, LocalDate endDate, int size) {
        return themeJpaRepository.findTopReservedThemesSince(startDate, endDate, size);
    }

    @Override
    public Theme findById(Long id) {
        return themeJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id 에 해당하는 예약 시각이 존재하지 않습니다."));
    }
}
