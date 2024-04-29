package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.ReservationRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.service.exception.OperationNotAllowedException;
import roomescape.service.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse addTheme(ThemeRequest request) {
        Theme theme = request.toTheme();
        Theme savedTheme = themeRepository.save(theme);

        return ThemeResponse.from(savedTheme);
    }

    public void deleteThemeById(Long id) {
        findValidatedTheme(id);
        boolean exist = reservationRepository.existByThemeId(id);
        if (exist) {
            throw new OperationNotAllowedException("해당 테마에 예약이 존재하기 때문에 삭제할 수 없습니다.");
        }

        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> getMostReservedThemes() {
        List<Theme> themes = themeRepository.findMostReservedThemesWithinDays(7, 10);

        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    private Theme findValidatedTheme(Long id) { //todo: private 함수 위치 통일감있게 가져가기
        return themeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 테마를 찾을 수 없습니다."));
    }
}
