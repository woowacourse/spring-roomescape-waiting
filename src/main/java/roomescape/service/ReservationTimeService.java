package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.dto.business.ReservationTimeCreationContent;
import roomescape.dto.business.ReservationTimeWithBookState;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationTimeService(
            ReservationTimeRepository timeRepository,
            ReservationRepository reservationRepository, WaitingRepository waitingRepository
    ) {
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationTimeResponse> findAllReservationTimes() {
        List<ReservationTime> reservationTimes = timeRepository.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public List<ReservationTimeWithBookState> findReservationTimesWithBookState(long themeId, LocalDate date) {
        List<ReservationTime> allTimes = timeRepository.findAllOrderByStartAt();
        List<ReservationTime> bookedTimes = timeRepository.findReservationTimesWithBookState(themeId, date);
        return calculateAllTimesWithBookedState(allTimes, bookedTimes);
    }

    public ReservationTimeResponse addReservationTime(ReservationTimeCreationContent request) {
        validateDuplicateTime(request.startAt());
        ReservationTime reservationTime = ReservationTime.createWithoutId(request.startAt());
        ReservationTime savedReservationTime = timeRepository.save(reservationTime);
        return new ReservationTimeResponse(savedReservationTime);
    }

    public void deleteReservationTimeById(Long timeId) {
        ReservationTime reservationTime = getReservationTimeById(timeId);
        validateReservationInTime(reservationTime);
        validateWaitingInTime(reservationTime);
        timeRepository.deleteById(timeId);
    }

    private void validateDuplicateTime(LocalTime startAt) {
        boolean alreadyExistTime = timeRepository.existsByStartAt(startAt);
        if (alreadyExistTime) {
            throw new BadRequestException("중복된 예약시간입니다.");
        }
    }

    private void validateReservationInTime(ReservationTime reservationTime) {
        if (reservationRepository.existsByReservationTime(reservationTime)) {
            throw new BadRequestException("이미 예약이 존재하는 예약 시간입니다.");
        }
    }

    private void validateWaitingInTime(ReservationTime reservationTime) {
        if (waitingRepository.existsByTime(reservationTime)) {
            throw new BadRequestException("이미 예약 대기가 존재하는 예약 시간입니다.");
        }
    }

    private ReservationTime getReservationTimeById(Long reservationTimeId) {
        return timeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new NotFoundException("ID에 해당하는 예약시간은 존재하지 않습니다."));
    }

    private List<ReservationTimeWithBookState> calculateAllTimesWithBookedState(
            List<ReservationTime> allTimes,
            List<ReservationTime> bookedTimes
    ) {
        List<ReservationTimeWithBookState> allTimesWithBookState = new ArrayList<>();
        for (ReservationTime time : allTimes) {
            if (bookedTimes.contains(time)) {
                allTimesWithBookState.add(new ReservationTimeWithBookState(time.getId(), time.getStartAt(), true));
                continue;
            }
            allTimesWithBookState.add(new ReservationTimeWithBookState(time.getId(), time.getStartAt(), false));
        }
        return allTimesWithBookState;
    }
}
