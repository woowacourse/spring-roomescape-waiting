package roomescape.theme.repository.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "repository.strategy", havingValue = "jpa")
public class JpaThemeRepositoryComposite implements ThemeRepository {
    private final JpaThemeRepository jpaThemeRepository;

    public JpaThemeRepositoryComposite(JpaThemeRepository jpaThemeRepository) {
        this.jpaThemeRepository = jpaThemeRepository;
    }

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
    public List<Theme> findTopByReservationCountDesc(LocalDate fromDate, LocalDate toDate, long limit) {
        return jpaThemeRepository.findTopByReservationCountDesc(fromDate, toDate, limit);
    }
}
