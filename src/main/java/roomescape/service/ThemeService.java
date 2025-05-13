package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.dto.theme.ThemeCreateRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.ConstrainedDataException;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public ThemeResponse createTheme(final ThemeCreateRequest requestDto) {
        Theme requestTheme = requestDto.createWithoutId();
        try {
            Theme savedTheme = themeRepository.save(requestTheme)
                    .orElseThrow(() -> new IllegalStateException("[ERROR] 테마를 저장할 수 없습니다. 관리자에게 문의해 주세요."));

            return ThemeResponse.from(savedTheme);
        } catch (DuplicateKeyException e) {
            throw new DuplicateContentException("[ERROR] 이미 동일한 이름의 테마가 존재합니다.");
        }
    }

    public List<ThemeResponse> findAllThemes() {
        List<Theme> allTheme = themeRepository.findAll();
        return allTheme.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void deleteThemeById(final Long id) {
        try {
            int deletedThemeCount = themeRepository.deleteById(id);

            if (deletedThemeCount == 0) {
                throw new NotFoundException("[ERROR] 등록된 테마만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
            }
        } catch (DataIntegrityViolationException e) {
            throw new ConstrainedDataException("[ERROR] 해당 테마에 예약 기록이 존재합니다. 예약을 먼저 삭제해 주세요.");
        }
    }

    public List<ThemeResponse> findPopularThemes() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);
        List<Theme> mostReservedThemes = themeRepository.findMostReservedByDateRange(start, end);
        return mostReservedThemes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
