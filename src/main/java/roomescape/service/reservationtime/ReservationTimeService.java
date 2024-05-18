package roomescape.service.reservationtime;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.exception.time.DuplicatedTimeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.exception.time.ReservationReferencedTimeException;
import roomescape.service.reservationtime.dto.ReservationTimeAvailableListResponse;
import roomescape.service.reservationtime.dto.ReservationTimeAvailableResponse;
import roomescape.service.reservationtime.dto.ReservationTimeListResponse;
import roomescape.service.reservationtime.dto.ReservationTimeRequest;
import roomescape.service.reservationtime.dto.ReservationTimeResponse;

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
    public ReservationTimeListResponse findAllReservationTime() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return new ReservationTimeListResponse(reservationTimes.stream()
                .map(ReservationTimeResponse::new)
                .toList());
    }

    @Transactional(readOnly = true)
    public ReservationTimeAvailableListResponse findAllAvailableReservationTime(LocalDate date, long themeId) {
        List<Long> bookedTimeIds = reservationRepository.findTimeIdByDateAndThemeId(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return new ReservationTimeAvailableListResponse(reservationTimes.stream()
                .map(time -> toAvailableReservationTimeResponse(time, bookedTimeIds))
                .toList());
    }

    private ReservationTimeAvailableResponse toAvailableReservationTimeResponse(
            ReservationTime time, List<Long> bookedTimeIds) {
        boolean alreadyBooked = time.isAlreadyBooked(bookedTimeIds);
        return new ReservationTimeAvailableResponse(time, alreadyBooked);
    }

    public ReservationTimeResponse saveReservationTime(ReservationTimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.getStartAt())) {
            throw new DuplicatedTimeException();
        }
        ReservationTime reservationTime = request.toReservationTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return new ReservationTimeResponse(savedReservationTime);
    }

    public void deleteReservationTime(long id) {
        ReservationTime reservationTime = findReservationTimeById(id);
        if (reservationRepository.existsByTimeId(reservationTime.getId())) {
            throw new ReservationReferencedTimeException();
        }
        reservationTimeRepository.delete(reservationTime);
    }

    private ReservationTime findReservationTimeById(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(NotFoundTimeException::new);
    }
}
