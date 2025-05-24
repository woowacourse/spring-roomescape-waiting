package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
@AllArgsConstructor
public class ThemeRepositoryImpl implements ThemeRepository {

    private final ThemeJpaRepository themeJpaRepository;

    @Override
    public Theme save(final Theme theme) {
        return themeJpaRepository.save(theme);
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        return themeJpaRepository.findById(id);
    }

    @Override
    public List<Theme> findAll() {
        return themeJpaRepository.findAll();
    }

    @Override
    public List<Theme> findAllOrderByRank(final LocalDate from, final LocalDate to, final int size) {
        return themeJpaRepository.findAllOrderByRank(from, to, size);
    }

    @Override
    public void delete(final Theme theme) {
        themeJpaRepository.delete(theme);
    }
}
