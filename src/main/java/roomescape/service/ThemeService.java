package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.theme.Name;
import roomescape.model.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.ThemeDto;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {

    private static final int COUNT_OF_DAY = 7;
    private static final int COUNT_OF_RANKING = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Theme> findAllThemes() {
        return themeRepository.findAll();
    }

    public Theme saveTheme(ThemeDto themeDto) {
        validateDuplication(themeDto);
        Theme theme = themeDto.toTheme();
        return themeRepository.save(theme);
    }

    public void deleteTheme(long id) {
        validateExistence(id);
        validateDependence(id);
        themeRepository.deleteById(id);
    }

    public List<Theme> findPopularThemes() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusDays(1 + COUNT_OF_DAY);
        LocalDate endDate = now.minusDays(1);
        return themeRepository.findRankingByDate(startDate, endDate, COUNT_OF_RANKING);
    }

    private void validateDuplication(ThemeDto themeDto) {
        Name name = themeDto.getName();
        boolean isExistName = themeRepository.existsByName(name);
        if (isExistName) {
            throw new DuplicatedException("[ERROR] 테마의 이름은 중복될 수 없습니다.");
        }
    }

    private void validateExistence(Long id) {
        boolean isNotExist = !themeRepository.existsById(id);
        if (isNotExist) {
            throw new NotFoundException("[ERROR] 존재하지 않는 테마입니다.");
        }
    }

    private void validateDependence(Long id) {
        boolean isExist = reservationRepository.existsByReservationInfo_ThemeId(id);
        if (isExist) {
            throw new BadRequestException("[ERROR] 해당 테마를 사용하고 있는 예약이 있습니다.");
        }
    }
}
