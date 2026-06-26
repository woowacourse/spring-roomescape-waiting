package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.PopularThemeResult;
import roomescape.service.dto.ThemeCreateCommand;
import roomescape.service.dto.ThemeResult;
import roomescape.service.exception.ThemeConflictException;
import roomescape.service.exception.ThemeInUseException;
import roomescape.service.exception.ThemeNotFoundException;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(
            ThemeRepository themeRepository,
            ReservationRepository reservationRepository
    ) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ThemeResult> findAll() {
        return themeRepository.findAll().stream()
                .map(ThemeResult::from)
                .toList();
    }

    public ThemeResult create(ThemeCreateCommand command) {
        if (themeRepository.existsByName(command.name())) {
            throw new ThemeConflictException("이미 등록된 테마 이름입니다: " + command.name());
        }
        Theme saved = themeRepository.save(
                new Theme(null, command.name(), command.description(), command.thumbnailUrl())
        );
        return ThemeResult.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!themeRepository.existsById(id)) {
            throw new ThemeNotFoundException("존재하지 않는 테마입니다: themeId=" + id);
        }
        reservationRepository.executeWithThemeLock(id, (lockedTheme, writer) -> {
            if (reservationRepository.existsByThemeId(id)) {
                throw new ThemeInUseException(
                        "예약이 존재하는 테마는 삭제할 수 없습니다: themeId=" + id);
            }
            themeRepository.deleteById(id);
            return null;
        });
    }

    public List<PopularThemeResult> findPopular() {
        return themeRepository.findPopular().stream()
                .map(p -> new PopularThemeResult(
                        ThemeResult.from(p.theme()),
                        p.reservationCount()))
                .toList();
    }

}
