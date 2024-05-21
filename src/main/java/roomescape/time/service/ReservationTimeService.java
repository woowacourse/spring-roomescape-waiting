package roomescape.time.service;

import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.exception.BadRequestException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.ReservationTimeCreateRequest;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.time.dto.TimeBookedResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public TimeBookedResponse createTime(ReservationTimeCreateRequest request) {
        ReservationTime reservationTime = request.toReservationTime();

        validateDuplicated(reservationTime);

        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return TimeBookedResponse.from(savedReservationTime);
    }

    private void validateDuplicated(ReservationTime reservationTime) {
        Boolean isExists = reservationTimeRepository.existsByStartAt(reservationTime.getStartAt());
        if (isExists) {
            throw new BadRequestException("중복된 예약 시간입니다.");
        }
    }

    public List<TimeBookedResponse> readReservationTimes() {
        return reservationTimeRepository.findAll().stream()
                .map(TimeBookedResponse::from)
                .toList();
    }

    public List<TimeBookedResponse> readReservationTimes(LocalDate date, Long themeId) {
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, themeId);
        Set<Long> alreadyBookedTimes = reservations.stream()
                .map(reservation -> reservation.getTime().getId())
                .collect(Collectors.toSet());

        return reservationTimeRepository.findAll().stream()
                .map(time -> createTimeResponse(time, alreadyBookedTimes))
                .toList();
    }

    private TimeBookedResponse createTimeResponse(ReservationTime time, Set<Long> alreadyBookedTimes) {
        boolean alreadyBooked = alreadyBookedTimes.contains(time.getId());
        return TimeBookedResponse.of(time, alreadyBooked);
    }

    public TimeBookedResponse readReservationTime(Long id) {
        ReservationTime reservationTime = reservationTimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약 시간입니다."));
        return TimeBookedResponse.from(reservationTime);
    }

    public void deleteTime(Long id) {
        validateReservationExists(id);
        reservationTimeRepository.deleteById(id);
    }

    private void validateReservationExists(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new BadRequestException("해당 시간대에 예약이 존재합니다.");
        }
    }
}
