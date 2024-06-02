package roomescape.service;

import static roomescape.exception.RoomescapeExceptionCode.CANNOT_DELETE_TIME_REFERENCED_BY_RESERVATION;
import static roomescape.exception.RoomescapeExceptionCode.TIME_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.AvailableReservationTimeResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(
            ReservationTimeRepository reservationTimeRepository,
            ReservationRepository reservationRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResponse> findReservationTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse createReservationTime(ReservationTimeRequest request) {
        ReservationTime reservationTime = request.toReservationTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponse.from(savedReservationTime);
    }

    public void deleteReservationTimeById(Long id) {
        boolean exist = reservationRepository.existsByReservationTimeId(id);
        if (exist) {
            throw new RoomescapeException(CANNOT_DELETE_TIME_REFERENCED_BY_RESERVATION);
        }
        reservationTimeRepository.delete(getReservationTime(id));
    }

    public List<AvailableReservationTimeResponse> findReservationTimesWithBookedStatus(LocalDate date, Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<ReservationTime> reservedTimes = reservationRepository.findAllByDateAndThemeId(date, themeId).stream()
                .map(Reservation::getReservationTime)
                .toList();

        return reservationTimes.stream()
                .map(reservationTime -> AvailableReservationTimeResponse.from(
                        reservationTime,
                        reservedTimes.contains(reservationTime)
                ))
                .toList();
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(TIME_NOT_FOUND));
    }
}
