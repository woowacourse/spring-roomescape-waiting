package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationRepository;
import roomescape.dao.ThemeRepository;
import roomescape.domain.AvailableTime;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationConflictException;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<Theme> getPopularThemes(int size, LocalDate startDate, LocalDate endDate) {
        return themeRepository.findPopularThemes(size, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Theme> getAllThemes() {
        return themeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AvailableTime> getAvailableTimes(long themeId, LocalDate date) {
        return themeRepository.findAvailableTimesForTheme(themeId, date).stream()
                .map(row -> new AvailableTime(new ReservationTime(row.getId(), row.getStartAt()), row.isAvailable()))
                .toList();
    }

    @Transactional
    public long save(String name, String description, String thumbnailUrl) {
        return themeRepository.save(new Theme(null, name, description, thumbnailUrl)).getId();
    }

    @Transactional
    public void delete(long id) {
        if (reservationRepository.existsByTheme_Id(id)) {
            throw new ReservationConflictException("예약에 사용 중인 테마는 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }
}
