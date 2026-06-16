package roomescape.time.service;

import static roomescape.date.exception.ReservationDateErrorInformation.DATE_NOT_FOUND;
import static roomescape.theme.exception.ThemeErrorInformation.THEME_NOT_FOUND;
import static roomescape.time.exception.ReservationTimeErrorInformation.TIME_ALREADY_EXISTS;
import static roomescape.time.exception.ReservationTimeErrorInformation.TIME_NOT_FOUND;
import static roomescape.time.exception.ReservationTimeErrorInformation.TIME_STATUS_UPDATE_FAILED;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.date.domain.ReservationDate;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.controller.dto.request.ReservationTimeSaveDto;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.ReservationTimeException;
import roomescape.time.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationDateRepository reservationDateRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public List<ReservationTime> readAll() {
        return reservationTimeRepository.findAll();
    }

    public List<ReservationTime> readAvailableTimes(Long dateId, Long themeId) {
        ReservationDate reservationDate = reservationDateRepository.findById(dateId)
            .orElseThrow(() -> new ReservationTimeException(DATE_NOT_FOUND));
        Theme theme = themeRepository.findById(themeId)
            .orElseThrow(() -> new ThemeException(THEME_NOT_FOUND));
        return reservationTimeRepository.findAllByIsActiveTrue();
    }

    @Transactional
    public ReservationTime register(ReservationTimeSaveDto dto) {
        validateDuplicateTimeExist(dto.startAt());
        return reservationTimeRepository.save(ReservationTime.create(dto.startAt()));
    }

    @Transactional
    public ReservationTime updateStatus(Long id, boolean isActive) {
        ReservationTime reservationTime = getReservationTime(id);
        reservationTime.updateStatus(isActive);
        return reservationTimeRepository.save(reservationTime);
    }

    private void validateIsUpdated(boolean isUpdated) {
        if (!isUpdated) {
            throw new ReservationTimeException(TIME_STATUS_UPDATE_FAILED);
        }
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
            .orElseThrow(() -> new ReservationTimeException(TIME_NOT_FOUND));
    }

    private void validateDuplicateTimeExist(LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new ReservationTimeException(TIME_ALREADY_EXISTS);
        }
    }

}
