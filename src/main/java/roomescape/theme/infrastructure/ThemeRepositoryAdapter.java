package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Repository
@AllArgsConstructor
public class ThemeRepositoryAdapter implements ThemeRepository {
    private final ThemeJpaRepository themeJpaRepository;

    @Override
    public boolean existsByName(String name) {
        return themeJpaRepository.existsByName(name);
    }

    @Override
    public Collection<Theme> findAll() {
        return themeJpaRepository.findAll();
    }

    @Override
    public List<Theme> findRankedByPeriod(LocalDate from, LocalDate to, int limit) {
        return themeJpaRepository.findRankedByPeriod(from, to, limit);
    }

    @Override
    public Theme save(Theme theme) {
        return themeJpaRepository.save(theme);
    }

    @Override
    public void deleteById(Long id) {
        themeJpaRepository.deleteById(id);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return themeJpaRepository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return themeJpaRepository.existsById(id);
    }
}
