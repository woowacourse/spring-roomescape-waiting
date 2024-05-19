package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationTime;
import roomescape.exception.reservation.TimeDuplicatedException;
import roomescape.exception.reservation.TimeUsingException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.reservation.ReservationTimeRequest;
import roomescape.service.dto.reservation.ReservationTimeResponse;
import roomescape.service.dto.time.AvailableTimeRequest;
import roomescape.service.dto.time.AvailableTimeResponse;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResponse> findAllReservationTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public List<AvailableTimeResponse> findAvailableReservationTimes(AvailableTimeRequest request) {
        LocalDate date = LocalDate.parse(request.getDate());
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        Set<ReservationTime> bookedTimes = reservationTimeRepository.findReservedTime(date, request.getThemeId());

        return allTimes.stream()
                .map(time -> parseAvailableTime(time, bookedTimes))
                .toList();
    }

    private AvailableTimeResponse parseAvailableTime(ReservationTime time, Set<ReservationTime> bookedTimes) {
        return new AvailableTimeResponse(
                new ReservationTimeResponse(time),
                bookedTimes.contains(time));
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
}
