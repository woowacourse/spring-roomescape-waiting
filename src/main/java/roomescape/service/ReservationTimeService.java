package roomescape.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationTime;
import roomescape.infrastructure.ReservationRepository;
import roomescape.infrastructure.ReservationTimeRepository;
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
        if (reservationTimeRepository.existsByStartAt(parsedTime)) {
            throw new IllegalArgumentException("이미 존재하는 예약 시간 정보 입니다.");
        }
    }

    public void delete(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new ReservationExistsException();
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationTimeAppResponse> findAll() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeAppResponse::from)
                .toList();
    }

    public List<BookableReservationTimeAppResponse> findAllWithBookAvailability(String date, Long themeId) {
        List<Reservation> reservations = reservationRepository.findAllByDateAndThemeId(new ReservationDate(date),
                themeId);
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
