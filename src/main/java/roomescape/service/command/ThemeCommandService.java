package roomescape.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.slot.Theme;
import roomescape.dto.theme.ThemeCreateRequestDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaThemeRepository;

@Service
@Transactional
public class ThemeCommandService {

    private final JpaThemeRepository themeRepository;
    private final JpaReservationRepository reservationRepository;

    public ThemeCommandService(JpaThemeRepository themeRepository, JpaReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponseDto createTheme(final ThemeCreateRequestDto requestDto) {
        if (themeRepository.existsByName(requestDto.name())) {
            throw new DuplicateContentException("해당 이름의 테마가 이미 존재합니다.");
        }
        Theme requestTheme = requestDto.createWithoutId();
        Theme savedTheme = themeRepository.save(requestTheme);
        return ThemeResponseDto.from(savedTheme);
    }

    public void deleteThemeById(final Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new IllegalStateException("이 테마는 이미 예약이 존재합니다. id : " + id);
        }
        if (!themeRepository.existsById(id)) {
            throw new NotFoundException("등록된 테마만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }

        themeRepository.deleteById(id);
    }
}
