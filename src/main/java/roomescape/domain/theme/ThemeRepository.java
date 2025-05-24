package roomescape.domain.theme;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.BaseRepository;
import roomescape.exception.NotFoundException;

public interface ThemeRepository extends BaseRepository<Theme, Long> {

    @Override
    Theme save(Theme entity);

    @Override
    Optional<Theme> findById(Long id);

    @Override
    Theme getById(Long id) throws NotFoundException;

    List<Theme> findAll();

    @Override
    List<Theme> findAll(Specification<Theme> specification);

    @Override
    boolean exists(Specification<Theme> spec);

    @Override
    void delete(Theme entity);

    @Override
    void deleteByIdOrElseThrow(Long id) throws NotFoundException;
}
