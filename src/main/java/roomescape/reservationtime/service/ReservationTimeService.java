package roomescape.reservationtime.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.ConflictException;
import roomescape.exception.ExceptionCause;
import roomescape.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponse createReservationTime(final ReservationTimeCreateRequest requestDto) {
        if (reservationTimeRepository.existsByStartAt(requestDto.startAt())) {
            throw new ConflictException(ExceptionCause.RESERVATION_TIME_DUPLICATE);
        }
        ReservationTime requestTime = requestDto.createWithoutId();
        ReservationTime savedTime = reservationTimeRepository.save(requestTime);
        return ReservationTimeResponse.from(savedTime);
    }

    public List<ReservationTimeResponse> findAllReservationTimes() {
        List<ReservationTime> allReservationTime = reservationTimeRepository.findAll();
        return allReservationTime.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(LocalDate date, Long themeId) {
        List<ReservationTime> allReservationTimes = reservationTimeRepository.findAll();
        List<Reservation> reservationsOnDate = reservationRepository.findByDateAndThemeId(date, themeId);

        List<ReservationTime> reservedTimes = reservationsOnDate.stream()
                .map(Reservation::getTime)
                .toList();

        return allReservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTimeResponse(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        reservedTimes.contains(reservationTime)
                ))
                .toList();
    }

    public void deleteReservationTimeById(final Long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new NotFoundException(ExceptionCause.RESERVATION_TIME_NOTFOUND);
        }
        if (reservationRepository.findByTimeId(id).isPresent()) {
            throw new BadRequestException(ExceptionCause.RESERVATION_TIME_EXIST);
        }
        reservationTimeRepository.deleteById(id);
    }
}
