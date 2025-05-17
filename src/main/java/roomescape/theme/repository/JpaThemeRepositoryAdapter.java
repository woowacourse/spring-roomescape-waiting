package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
public class JpaThemeRepositoryAdapter implements ThemeRepository {

    private final JpaThemeRepository jpaThemeRepository;

    public JpaThemeRepositoryAdapter(final JpaThemeRepository jpaThemeRepository) {
        this.jpaThemeRepository = jpaThemeRepository;
    }

    public List<Theme> findTop10PopularThemesWithinLastWeek(LocalDate fromDate, LocalDate toDate) {
        return jpaThemeRepository.findTop10PopularThemesWithinLastWeek(fromDate, toDate);
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

    public Optional<Theme> findById(Long id){
        return jpaThemeRepository.findById(id);
    }
}
