package roomescape.theme.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.DataNotFoundException;
import roomescape.theme.domain.Theme;

@RequiredArgsConstructor
@Repository
public class JpaThemeRepository implements ThemeRepositoryInterface {

    private final ThemeRepository themeRepository;

    @Override
    public boolean existsByName(final String name) {
        return themeRepository.existsByName(name);
    }

    @Override
    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    @Override
    public Theme findById(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("해당 테마 데이터가 존재하지 않습니다. id = " + id));
    }

    @Override
    public Theme save(final Theme theme) {
        return themeRepository.save(theme);
    }

    @Override
    public void deleteById(final Long id) {
        themeRepository.deleteById(id);
    }
}
