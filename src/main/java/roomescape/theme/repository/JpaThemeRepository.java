package roomescape.theme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
public interface JpaThemeRepository extends JpaRepository<Theme, Long>, ThemeRepository {
    
    @Override
    void deleteById(Long id);
}
