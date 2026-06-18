package roomescape.adapter.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.projection.PopularThemeProjection;

@Repository
public class ThemeRepositoryAdapter implements ThemeRepository {

    private final ThemeJpaRepository jpaRepository;

    public ThemeRepositoryAdapter(ThemeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Theme> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Theme save(Theme theme) {
        return jpaRepository.save(theme);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<PopularThemeProjection> findPopularBetween(LocalDate from, LocalDate to, int limit) {
        return jpaRepository.findPopularBetween(from, to, PageRequest.of(0, limit));
    }
}
