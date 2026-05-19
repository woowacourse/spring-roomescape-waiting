package roomescape.time.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.time.controller.dto.request.ReservationTimeSaveDto;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.ReservationTimeException;
import roomescape.time.repository.ReservationTimeRepository;

import java.time.LocalTime;
import java.util.List;

import static roomescape.time.exception.ReservationTimeErrorInformation.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public List<ReservationTime> readAll() {
        return reservationTimeRepository.findAll();
    }

    public List<ReservationTime> readAvailableTimes(Long dateId, Long themeId) {
        return reservationTimeRepository.findAvailableByDateIdAndThemeId(dateId, themeId);
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
        boolean isUpdated = reservationTimeRepository.updateStatus(reservationTime);
        validateIsUpdated(isUpdated);
        return reservationTime;
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
