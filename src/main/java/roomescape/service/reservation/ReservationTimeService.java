package roomescape.service.reservation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.exception.InvalidReservationException;
import roomescape.service.reservation.dto.AvailableReservationTimeResponse;
import roomescape.service.reservation.dto.ReservationTimeCreateRequest;
import roomescape.service.reservation.dto.ReservationTimeReadRequest;
import roomescape.service.reservation.dto.ReservationTimeResponse;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponse create(ReservationTimeCreateRequest reservationTimeCreateRequest) {
        validateDuplicated(reservationTimeCreateRequest);
        ReservationTime reservationTime = reservationTimeRepository.save(reservationTimeCreateRequest.toReservationTime());
        return new ReservationTimeResponse(reservationTime);
    }

    private void validateDuplicated(ReservationTimeCreateRequest reservationTimeCreateRequest) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeCreateRequest.startAt())) {
            throw new InvalidReservationException("이미 같은 시간이 존재합니다.");
        }
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public void deleteById(long id) {
        validateByReservation(id);
        reservationTimeRepository.deleteById(id);
    }

    private void validateByReservation(long id) {
        if (reservationRepository.existsByScheduleTimeId(id)) {
            throw new InvalidReservationException("해당 시간에 예약이 존재해서 삭제할 수 없습니다.");
        }
    }

    public List<AvailableReservationTimeResponse> findAvailableTimes(
            ReservationTimeReadRequest reservationTimeReadRequest) {
        List<Reservation> reservations = reservationRepository.findByScheduleDateAndThemeId(
                ReservationDate.of(reservationTimeReadRequest.date()), reservationTimeReadRequest.themeId());
        return reservationTimeRepository.findAll().stream()
                .map(time -> new AvailableReservationTimeResponse(time.getId(), time.getStartAt(),
                        isBooked(reservations, time)))
                .toList();
    }

    private boolean isBooked(List<Reservation> reservations, ReservationTime time) {
        return reservations.stream()
                .map(Reservation::getReservationTime)
                .anyMatch(time::isSame);
    }
}
