package roomescape.theme.infrastructure;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Repository
public class JpaThemeRepositoryAdapter implements ThemeRepository {

    private final JpaThemeRepository jpaThemeRepository;

    public JpaThemeRepositoryAdapter(final JpaThemeRepository jpaThemeRepository) {
        this.jpaThemeRepository = jpaThemeRepository;
    }

    public Page<Theme> findPopularThemes(final LocalDate fromDate, final LocalDate toDate, final Pageable pageable) {
        return jpaThemeRepository.findPopularThemes(fromDate, toDate, pageable);
    }

    public List<Theme> findAll() {
        return jpaThemeRepository.findAll();
    }

    public void deleteById(Long id) {
        jpaThemeRepository.deleteById(id);
    }


    public Theme save(Theme theme) {
        return jpaThemeRepository.save(theme);
    }

    public Optional<Theme> findById(Long id) {
        return jpaThemeRepository.findById(id);
    }
}
