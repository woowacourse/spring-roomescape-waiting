package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.ReservationTimeAvailabilities;
import roomescape.reservation.model.Theme;
import roomescape.reservation.dto.SaveThemeRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ThemeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ThemeService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<Theme> getThemes() {
        return themeRepository.findAll();
    }

    public Theme saveTheme(final SaveThemeRequest saveThemeRequest) {
        return themeRepository.save(saveThemeRequest.toTheme());
    }

    public void deleteTheme(final Long themeId) {
        validateReservationOfIncludeThemeExist(themeId);

        final int deletedDataCount = themeRepository.deleteById(themeId);

        if (deletedDataCount <= 0) {
            throw new NoSuchElementException("해당 id의 테마 정보가 존재하지 않습니다.");
        }
    }

    private void validateReservationOfIncludeThemeExist(final Long themeId) {
        if (reservationRepository.existByThemeId(themeId)) {
            throw new IllegalArgumentException("예약에 포함된 테마 정보는 삭제할 수 없습니다.");
        }
    }

    public ReservationTimeAvailabilities getAvailableReservationTimes(final LocalDate date, final Long themeId) {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final List<Reservation> reservations = reservationRepository.findAllByDateAndThemeId(date, themeId);

        return ReservationTimeAvailabilities.of(reservationTimes, reservations);
    }

    public List<Theme> getPopularThemes() {
        final ReservationDate startAt = new ReservationDate(LocalDate.now().minusDays(7));
        final ReservationDate endAt = new ReservationDate(LocalDate.now().minusDays(1));
        final int maximumThemeCount = 10;

        return themeRepository.findPopularThemes(startAt, endAt, maximumThemeCount);
    }
}
