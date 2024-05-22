package roomescape.service;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationTime;
import roomescape.exception.reservation.TimeDuplicatedException;
import roomescape.exception.reservation.TimeUsingException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.time.AvailableTimeRequest;
import roomescape.service.dto.time.AvailableTimeResponse;
import roomescape.service.dto.time.AvailableTimeResponses;
import roomescape.service.dto.time.ReservationTimeRequest;
import roomescape.service.dto.time.ReservationTimeResponse;

@Service
@Transactional
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAllReservationTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvailableTimeResponses findAvailableReservationTimes(AvailableTimeRequest request) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        Set<ReservationTime> bookedTimes = reservationTimeRepository
                .findReservedTimeByDateAndTheme(request.getDate(), request.getThemeId());

        return new AvailableTimeResponses(allTimes.stream()
                .map(time -> parseAvailableTime(time, bookedTimes))
                .toList());
    }

    public ReservationTimeResponse createReservationTime(ReservationTimeRequest request) {
        ReservationTime reservationTime = request.toReservationTime();
        if (reservationTimeRepository.existsByStartAt(reservationTime.getStartAt())) {
            throw new TimeDuplicatedException();
        }
        ReservationTime savedTime = reservationTimeRepository.save(reservationTime);
        return new ReservationTimeResponse(savedTime);
    }

    public void deleteReservationTime(long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new TimeUsingException();
        }
        reservationTimeRepository.deleteById(id);
    }

    private AvailableTimeResponse parseAvailableTime(ReservationTime time, Set<ReservationTime> bookedTimes) {
        return new AvailableTimeResponse(
                new ReservationTimeResponse(time),
                bookedTimes.contains(time));
    }
}
