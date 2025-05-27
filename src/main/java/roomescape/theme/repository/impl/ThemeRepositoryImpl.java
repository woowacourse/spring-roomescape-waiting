package roomescape.theme.repository.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JpaThemeRepository;
import roomescape.theme.repository.ThemeRepository;

@Repository
@RequiredArgsConstructor
// TODO : 구현체 이름 고민해보기
public class ThemeRepositoryImpl implements ThemeRepository {

    private final JpaThemeRepository jpaThemeRepository;

    @Override
    public List<Theme> findAll() {
        return jpaThemeRepository.findAll();
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return jpaThemeRepository.findById(id);
    }

    @Override
    public Theme save(Theme theme) {
        return jpaThemeRepository.save(theme);
    }

    @Override
    public void deleteById(Long id) {
        jpaThemeRepository.deleteById(id);
    }

    @Override
    public List<Theme> getRanking(ReservationDate startDate, ReservationDate endDate, Pageable pageable) {
        return jpaThemeRepository.findPopularThemes(startDate, endDate, pageable);
    }
}
