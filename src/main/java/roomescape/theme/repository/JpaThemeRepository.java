package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
public class JpaThemeRepository implements ThemeRepository {

    private final ThemeListCrudRepository themeListCrudRepository;

    public JpaThemeRepository(ThemeListCrudRepository themeListCrudRepository) {
        this.themeListCrudRepository = themeListCrudRepository;
    }

    @Override
    public Theme save(Theme theme) {
        return themeListCrudRepository.save(theme);
    }

    @Override
    public List<Theme> findAll() {
        return themeListCrudRepository.findAll();
    }

    @Override
    public List<Theme> findTopThemes(LocalDate from, LocalDate to, int limit) {
        PageRequest pageRequest = PageRequest.of(0, 10);
        return themeListCrudRepository.findTopThemes(from, to, pageRequest);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return themeListCrudRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        themeListCrudRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return themeListCrudRepository.existsByName(name);
    }
}
