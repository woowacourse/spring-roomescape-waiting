package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.exception.DuplicateReservationException;
import roomescape.repository.ReservationDao;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private final ReservationDao reservationDao;

    private void validateDuplicate(LocalDate date, Long timeId, Long themeId) {
        if (reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new DuplicateReservationException("중복된 예약이 존재합니다.");
        }
    }

    public Reservation create(String name, LocalDate date, long timeId, long themeId) {
        validateDuplicate(date, timeId, themeId);
        return reservationDao.save(name, date, timeId, themeId);
    }

    public void delete(long reservationId) {
        reservationDao.delete(reservationId);
    }
}
