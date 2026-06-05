package roomescape.reservationtime.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.application.port.out.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.application.dto.request.ReservationTimeSaveRequest;
import roomescape.reservationtime.application.dto.response.AvailableTimeFindResponse;
import roomescape.reservationtime.application.dto.response.ReservationTimeFindResponse;
import roomescape.reservationtime.application.dto.response.ReservationTimeSaveResponse;
import roomescape.reservationtime.application.dto.response.TimeInformation;
import roomescape.reservationtime.application.dto.response.TimeSlotStatus;
import roomescape.reservationtime.application.port.out.ReservationTimeRepository;
import roomescape.slot.application.SlotUsageValidator;
import roomescape.waiting.application.port.out.WaitingRepository;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {
    private final SlotUsageValidator slotUsageValidator;
    private final ReservationTimeAssembler reservationTimeAssembler;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationTimeSaveResponse save(ReservationTimeSaveRequest body) {
        validateAlreadyTimeNot(body.startAt());
        ReservationTime reservationTime = reservationTimeAssembler.assemble(body.startAt());
        return ReservationTimeSaveResponse.from(reservationTimeRepository.save(reservationTime));
    }

    public List<ReservationTimeFindResponse> findAll() {
        return ReservationTimeFindResponse.from(reservationTimeRepository.findAll());
    }

    public void delete(long id) {
        slotUsageValidator.validateTimeDeletable(id);
        reservationTimeRepository.deleteById(id);
    }

    public List<AvailableTimeFindResponse> findTimesByDateAndThemeId(LocalDate date, long themeId) {
        // slot에서 존재하는 시간 id 모두 조회
        List<ReservationTime> totalTimes = reservationTimeRepository.findTimesByDateAndThemeId(date, themeId);

        Set<Long> reservationTimeIds = reservationRepository.findTimeIdByDateAndThemeId(date, themeId);
        Set<Long> waitingTimeIds = waitingRepository.findTimeIdByDateAndThemeId(date, themeId);
        Set<Long> notAvailableTimeIds = new HashSet<>(reservationTimeIds);
        notAvailableTimeIds.addAll(waitingTimeIds);

        return totalTimes.stream()
                .map(time -> new AvailableTimeFindResponse(
                        new TimeInformation(time.id(), time.startAt()),
                        getStatus(time, notAvailableTimeIds)
                ))
                .toList();
    }

    private TimeSlotStatus getStatus(ReservationTime time, Set<Long> notAvailableTimeIds) {
        if (notAvailableTimeIds.contains(time.id())) {
            return TimeSlotStatus.WAITABLE;
        }
        return TimeSlotStatus.RESERVABLE;
    }

    private void validateAlreadyTimeNot(LocalTime startAt) {
        if (reservationTimeRepository.existsAlreadyTime(startAt)) {
            throw new EscapeRoomException(ErrorCode.RESERVATIONTIME_ALREADY_EXIST);
        }
    }
}
