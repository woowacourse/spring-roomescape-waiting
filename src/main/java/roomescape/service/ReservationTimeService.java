package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.service.exception.ReservationExistsException;
import roomescape.service.request.ReservationTimeAppRequest;
import roomescape.service.response.BookableReservationTimeAppResponse;
import roomescape.service.response.ReservationTimeAppResponse;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeAppResponse save(ReservationTimeAppRequest request) {
        ReservationTime newReservationTime = new ReservationTime(request.startAt());
        validateDuplication(newReservationTime.getStartAt());
        ReservationTime savedTime = reservationTimeRepository.save(newReservationTime);

        return ReservationTimeAppResponse.from(savedTime);
    }

    private void validateDuplication(LocalTime parsedTime) {
        if (reservationTimeRepository.isStartTimeExists(parsedTime)) {
            throw new IllegalArgumentException("이미 존재하는 예약 시간 정보 입니다.");
        }
    }

    public int delete(Long id) {
        if (reservationRepository.isTimeIdExists(id)) {
            throw new ReservationExistsException();
        }
        return reservationTimeRepository.deleteById(id);
    }

    public List<ReservationTimeAppResponse> findAll() {
        return reservationTimeRepository.findAll().stream()
            .map(ReservationTimeAppResponse::from)
            .toList();
    }

    public List<BookableReservationTimeAppResponse> findAllWithBookAvailability(LocalDate date, Long themeId) {
        List<Reservation> reservations = reservationRepository.findAllByDateAndThemeId(date, themeId);
        List<ReservationTime> reservedTimes = reservations.stream()
            .map(Reservation::getReservationTime)
            .toList();

        return reservationTimeRepository.findAll().stream()
            .map(time -> BookableReservationTimeAppResponse.of(time, isBooked(reservedTimes, time)))
            .toList();
    }

    private boolean isBooked(List<ReservationTime> reservedTimes, ReservationTime time) {
        return reservedTimes.stream()
            .anyMatch(reservationTime -> Objects.equals(reservationTime.getId(), time.getId()));
    }
}
