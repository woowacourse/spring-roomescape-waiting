package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.entity.Theme;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {
    Theme save(Theme theme); //TODO: 이미 구현되어있어서 나중에 삭제

    List<Theme> findAll(); //TODO: 이미 구현되어있어서 나중에 삭제

    Optional<Theme> findById(Long id); //TODO: 이미 구현되어있어서 나중에 삭제

    boolean existsReservationByThemeId(long id); //TODO: 메서드명 수정 필요

    void deleteById(Long id); //TODO: 이미 구현되어있어서 나중에 삭제
}
