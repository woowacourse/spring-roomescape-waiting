package roomescape.service;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.AvailableTimeFindRequest;
import roomescape.controller.dto.request.ReservationTimeCreateRequest;
import roomescape.domain.reservation.ReservationTime;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final SlotRepository slotRepository;

    @Transactional
    public ReservationTime create(ReservationTimeCreateRequest request) {
        ReservationTime reservationTime = ReservationTime.of(request.getStartAt());
        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public List<ReservationTime> findAvailable(AvailableTimeFindRequest request, LocalDate now) {
        if (now.isAfter(request.getDate())) {
            throw new RoomEscapeException(ErrorCode.PAST_DATE_NOT_ALLOWED);
        }
        return reservationTimeRepository.findAvailableByDateAndTheme(request.getDate(), request.getThemeId());
    }

    @Transactional
    public void delete(long reservationTimeId) {
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new RoomEscapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        slotRepository.findByTime(reservationTime)
                .ifPresent(slot -> {
                    throw new RoomEscapeException(ErrorCode.RESERVATION_TIME_IN_USE);
                });

        reservationTimeRepository.deleteById(reservationTimeId);
    }
}
