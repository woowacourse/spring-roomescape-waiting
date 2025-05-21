package roomescape.theme.service;

import org.springframework.stereotype.Service;
import roomescape.common.exception.BusinessException;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.presentation.dto.ThemeRequest;

@Service
public class ThemeCommandService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeCommandService(final ThemeRepository themeRepository, final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public Theme save(final ThemeRequest request) {
        return themeRepository.save(new Theme(request.name(), request.description(), request.thumbnail()));
    }

    public void deleteById(final Long id) {
        validateExistsIdToDelete(id);
        validateExistsTheme(id);

        themeRepository.deleteById(id);
    }

    private void validateExistsTheme(final Long id) {
        if (!themeRepository.existsById(id)) {
            throw new BusinessException("해당 테마가 존재하지 않습니다.");
        }
    }

    private void validateExistsIdToDelete(final Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new BusinessException("해당 테마의 예약이 존재해서 삭제할 수 없습니다.");
        }
    }
}
