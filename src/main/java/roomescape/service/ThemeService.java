package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.ReferencedDataException;

import java.util.List;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public ThemeResponse create(ThemeRequest themeRequest) {
        Theme theme = new Theme(null, themeRequest.name(), themeRequest.description(), themeRequest.url());
        Long id = themeRepository.insert(theme);
        return ThemeResponse.from(new Theme(id, themeRequest.name(), themeRequest.description(), themeRequest.url()));
    }

    public List<ThemeResponse> findAll() {
        return themeRepository.findAllTheme().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findPopularTheme() {
        return themeRepository.findAllByTopTheme().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void delete(Long id) {
        try {
            themeRepository.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReferencedDataException("해당 테마에 예약이 존재하여 삭제할 수 없습니다.");
        }
    }
}
