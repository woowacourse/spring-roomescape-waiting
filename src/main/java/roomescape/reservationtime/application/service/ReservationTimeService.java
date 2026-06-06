package roomescape.reservationtime.application.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.ConflictException;
import roomescape.global.NotFoundException;
import roomescape.reservationtime.application.dto.ReservationTimeCreateCommand;
import roomescape.reservationtime.exception.ReservationTimeErrorMessage;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.reservationtime.presentation.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.presentation.dto.ReservationTimeResponse;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;

    @Transactional(readOnly = true)
    public ReservationTimeResponse findById(Long timeId) {
        return ReservationTimeResponse.from(timeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException(ReservationTimeErrorMessage.TIME_NOT_FOUND, timeId)));
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAll() {
        return timeRepository.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAvailableTimes(Long themeId, LocalDate date) {
        return timeRepository.findAvailableByThemeAndDate(themeId, date).stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse save(ReservationTimeCreateCommand request) {
        validateDuplicateTime(request.startAt());
        ReservationTime savedTime = timeRepository.save(request.toEntity());
        return ReservationTimeResponse.from(savedTime);
    }

    public int delete(Long id) {
        return timeRepository.delete(id);
    }

    private void validateDuplicateTime(LocalTime startAt) {
        if (timeRepository.existsByStartAt(startAt)) {
            throw new ConflictException(ReservationTimeErrorMessage.DUPLICATE_TIME);
        }
    }
}
