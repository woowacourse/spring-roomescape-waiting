package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.domain.policy.PopularThemePolicy;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.application.dto.result.PopularThemeResult;
import roomescape.application.dto.command.ThemeCreateCommand;
import roomescape.application.dto.result.ThemeResult;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final PopularThemePolicy popularThemePolicy;

    public ThemeService(
            ThemeRepository themeRepository,
            ReservationRepository reservationRepository,
            PopularThemePolicy popularThemePolicy
    ) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.popularThemePolicy = popularThemePolicy;
    }


    public List<ThemeResult> findAll() {
        return themeRepository.findAll().stream()
                .map(ThemeResult::from)
                .toList();
    }

    @Transactional
    public ThemeResult create(ThemeCreateCommand command) {
        Theme theme = Theme.create(command.getName(), command.getDescription(), command.getThumbnail());
        Theme saved = themeRepository.save(theme);
        return ThemeResult.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        validateNotInUse(id);
        themeRepository.deleteById(id);
    }

    public List<PopularThemeResult> findPopular() {
        LocalDate today = popularThemePolicy.today();

        return themeRepository.findPopularBetween(
                        popularThemePolicy.from(today),
                        popularThemePolicy.to(today),
                        popularThemePolicy.limit())
                .stream()
                .map(PopularThemeResult::from)
                .toList();
    }

    private void validateNotInUse(Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new BusinessRuleViolationException(
                    "예약이 존재하는 테마는 삭제할 수 없습니다."
            );
        }
    }
}
