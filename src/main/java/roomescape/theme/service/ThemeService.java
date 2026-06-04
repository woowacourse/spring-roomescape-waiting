package roomescape.theme.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Theme create(String name, String description, String thumbnail) {
        Theme theme = new Theme(name, description, thumbnail);

        return save(theme);
    }

    @Transactional(readOnly = true)
    public List<Theme> list() {
        return themeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Theme> findPopularThemes(int days, LocalDate now, int size) {
        LocalDate startDate = now.minusDays(days);
        LocalDate endDate = now.minusDays(1);

        return themeRepository.findTopThemesByReservationCount(startDate, endDate, size);
    }

    @Transactional
    public void delete(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new ConflictException("예약이 존재하는 테마는 삭제할 수 없습니다. 먼저 해당 예약들을 삭제해주세요.");
        }

        deleteTheme(id);
    }

    private Theme save(Theme theme) {
        try {
            return themeRepository.save(theme);
        } catch (DuplicateKeyException exception) {
            throw new ConflictException("이미 등록된 테마 이름입니다. 다른 이름을 입력해주세요.");
        }
    }

    private void deleteTheme(Long id) {
        try {
            themeRepository.deleteById(id);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("예약이 존재하는 테마는 삭제할 수 없습니다. 먼저 해당 예약들을 삭제해주세요.");
        }
    }
}
