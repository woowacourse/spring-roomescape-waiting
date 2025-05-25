package roomescape.infrastructure.jpa.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.vo.Id;

public interface JpaThemeDao extends JpaRepository<Theme, Id> {
}
