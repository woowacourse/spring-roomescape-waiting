package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.exception.BadRequestException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.request.ReservationAvailabilityTimeRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.response.ListResponse;
import roomescape.service.dto.response.ReservationAvailabilityTimeResponse;
import roomescape.service.dto.response.ReservationTimeResponse;

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
    public ListResponse<ReservationTimeResponse> findAll() {
        List<ReservationTimeResponse> responses = reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();

        return new ListResponse<>(responses);
    }

    public ReservationTimeResponse save(ReservationTimeRequest reservationTimeRequest) {
        ReservationTime reservationTime = reservationTimeRequest.toReservationTime();

        validateDuplicatedTime(reservationTime.getStartAt());

        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    public void deleteById(long id) {
        reservationTimeRepository.deleteById(id);
    }

    private void validateDuplicatedTime(LocalTime startAt) {
        boolean exists = reservationTimeRepository.existsByStartAt(startAt);
        if (exists) {
            throw new BadRequestException("중복된 시간을 생성할 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public ListResponse<ReservationAvailabilityTimeResponse> findReservationAvailabilityTimes(
            ReservationAvailabilityTimeRequest timeRequest) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<Reservation> reservations = reservationRepository.findByThemeId(timeRequest.themeId());

        List<ReservationAvailabilityTimeResponse> responses = reservationTimes.stream()
                .map(reservationTime -> {
                    boolean isBooked = isReservationBooked(reservations, timeRequest.date(), reservationTime);
                    return ReservationAvailabilityTimeResponse.from(reservationTime, isBooked);
                })
                .toList();

        return new ListResponse<>(responses);
    }

    private boolean isReservationBooked(
            List<Reservation> reservations,
            LocalDate date,
            ReservationTime reservationTime) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.hasDateTime(date, reservationTime));
    }
}
