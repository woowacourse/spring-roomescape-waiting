package roomescape.theme.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(name = "repository.strategy", havingValue = "jpa")
public class JpaThemeRepositoryComposite implements ThemeRepository {

    private final JpaThemeRepository jpaThemeRepository;

    @Override
    public Theme save(Theme theme) {
        return jpaThemeRepository.save(theme);
    }

    @Override
    public List<Theme> findAll() {
        return jpaThemeRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpaThemeRepository.deleteById(id);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return jpaThemeRepository.findById(id);
    }

    @Override
    public List<Theme> findTopReservedThemesInPeriod(LocalDate from, LocalDate to, int size) {
        return jpaThemeRepository.findTopReservedThemesInPeriod(from, to, size);
    }
}
