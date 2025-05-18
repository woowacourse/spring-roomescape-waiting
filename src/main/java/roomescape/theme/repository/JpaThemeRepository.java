package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
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
    public Optional<Theme> findById(final Long id) {
        return themeRepository.findById(id);
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
