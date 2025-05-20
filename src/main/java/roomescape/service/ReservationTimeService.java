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
import roomescape.exception.local.AlreadyReservedTimeException;
import roomescape.exception.local.DuplicateReservationException;
import roomescape.exception.local.NotFoundReservationTimeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
@Transactional
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(
            ReservationTimeRepository timeRepository,
            ReservationRepository reservationRepository
    ) {
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
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
        return getAllTimesWithBookedState(allTimes, bookedTimes);
    }

    public ReservationTimeResponse addReservationTime(ReservationTimeCreationContent request) {
        validateDuplicateTime(request.startAt());
        ReservationTime reservationTime = ReservationTime.createWithoutId(request.startAt());
        ReservationTime savedReservationTime = timeRepository.save(reservationTime);
        return new ReservationTimeResponse(savedReservationTime);
    }

    public void deleteReservationTimeById(Long id) {
        ReservationTime reservationTime = loadReservationTimeById(id);
        validateReservationInTime(reservationTime);
        timeRepository.deleteById(id);
    }

    private void validateDuplicateTime(LocalTime startAt) {
        boolean alreadyExistTime = timeRepository.existsByStartAt(startAt);
        if (alreadyExistTime) {
            throw new DuplicateReservationException();
        }
    }

    private void validateReservationInTime(ReservationTime reservationTime) {
        if (reservationRepository.existsByReservationTime(reservationTime)) {
            throw new AlreadyReservedTimeException();
        }
    }

    private ReservationTime loadReservationTimeById(Long reservationTimeId) {
        return timeRepository.findById(reservationTimeId)
                .orElseThrow(NotFoundReservationTimeException::new);
    }

    private List<ReservationTimeWithBookState> getAllTimesWithBookedState(
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
