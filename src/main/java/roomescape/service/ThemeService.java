package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.theme.Theme;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.ThemeDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ThemeService {

    private static final int COUNT_OF_DAY = 7;
    private static final int COUNT_OF_RANKING = 10;

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<Theme> findAllThemes() {
        return themeRepository.findAllThemes();
    }

    public Theme saveTheme(ThemeDto themeDto) {
        validateDuplication(themeDto.getName());
        Theme theme = Theme.from(themeDto);
        Optional<Theme> savedTheme = themeRepository.saveTheme(theme);
        return savedTheme.orElseThrow(() -> new BadRequestException("[ERROR] 데이터가 저장되지 않았습니다."));
    }

    public void deleteTheme(long id) {
        validateExistence(id);
        themeRepository.deleteThemeById(id);
    }

    public List<Theme> findPopularThemes() {
        LocalDate startDate = LocalDate.now().minusDays(1 + COUNT_OF_DAY);
        LocalDate endDate = LocalDate.now().minusDays(1);
        return themeRepository.findThemeRankingByDate(startDate, endDate, COUNT_OF_RANKING);
    }

    private void validateDuplication(String name) {
        boolean isExistName = themeRepository.isExistThemeByName(name);
        if (isExistName) {
            throw new DuplicatedException("[ERROR] 테마의 이름은 중복될 수 없습니다.");
        }
    }

    private void validateExistence(Long id) {
        boolean isNotExist = !themeRepository.isExistThemeById(id);
        if (isNotExist) {
            throw new NotFoundException("[ERROR] 존재하지 않는 테마입니다.");
        }
    }
}
