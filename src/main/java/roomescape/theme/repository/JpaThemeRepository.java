package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
public interface JpaThemeRepository extends JpaRepository<Theme, Long>, ThemeRepository {

    @Override
    List<Theme> findAll();

    @Override
    Optional<Theme> findById(Long id);

    @Override
    Theme save(Theme theme);

    @Override
    void deleteById(Long id);
}
