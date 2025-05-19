package roomescape.theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class ThemeRepositoryFacadeImpl implements ThemeRepositoryFacade {

    private final ThemeRepository themeRepository;

    @Override
    public Theme save(final Theme theme) {
        return themeRepository.save(theme);
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        return themeRepository.findById(id);
    }

    @Override
    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    @Override
    public List<Theme> findAllOrderByRank(final LocalDate from, final LocalDate to, final int size) {
        return themeRepository.findAllOrderByRank(from, to, size);
    }

    @Override
    public void delete(final Theme theme) {
        themeRepository.delete(theme);
    }
}
