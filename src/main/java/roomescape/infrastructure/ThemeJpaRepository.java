package roomescape.infrastructure;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.NotFoundException;

public interface ThemeJpaRepository extends ThemeRepository, Repository<Theme, Long> {

    @Override
    default Theme getById(final Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다. id : " + id));
    }

    @Modifying
    @Query("DELETE FROM THEME t WHERE t.id = :id")
    @Transactional
    int deleteByIdAndCount(@Param("id") final Long id);

    @Override
    @Transactional
    default void deleteByIdOrElseThrow(final Long id) {
        var deletedCount = deleteByIdAndCount(id);
        if (deletedCount == 0) {
            throw new NotFoundException("존재하지 않는 테마입니다. id : " + id);
        }
    }
}
