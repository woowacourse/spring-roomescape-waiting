package roomescape.theme.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import roomescape.theme.application.port.out.ThemeRepository;
import roomescape.theme.domain.Theme;

@Repository
@RequiredArgsConstructor
public class JpaThemeRepository implements ThemeRepository {
    private static final int POPULAR_THEME_LIMIT = 10;

    private final SpringDataThemeRepository repository;

    @Override
    public Theme save(Theme domain) {
        return repository.save(domain);
    }

    @Override
    public void deleteById(long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Theme> findThemesBySlotDate(LocalDate date) {
        return repository.findThemesBySlotDate(date);
    }

    @Override
    public List<Theme> findPopularThemeByCurrentDate(LocalDate currentDate) {
        LocalDate startDate = currentDate.minusDays(7);
        return repository.findPopularThemeByCurrentDate(
                startDate,
                currentDate,
                PageRequest.of(0, POPULAR_THEME_LIMIT)
        );
    }

    @Override
    public Optional<Theme> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<Theme> findAll() {
        return repository.findAll();
    }

    @Override
    public boolean existsAlreadyTheme(String name) {
        return repository.existsByName(name);
    }
}
