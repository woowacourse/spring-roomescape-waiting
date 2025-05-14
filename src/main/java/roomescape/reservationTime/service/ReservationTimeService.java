package roomescape.reservationTime.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.dto.request.ReservationTimeRequest;
import roomescape.reservationTime.dto.request.TimeConditionRequest;
import roomescape.reservationTime.dto.response.ReservationTimeResponse;
import roomescape.reservationTime.dto.response.TimeConditionResponse;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(final JpaReservationRepository reservationRepository,
                                  final JpaReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTimeResponse createReservationTime(final ReservationTimeRequest request) {
        ReservationTime reservationTime = ReservationTime.createWithoutId(request.startAt());
        ReservationTime save = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponse.from(save);
    }

    public void deleteReservationTimeById(final Long id) {
        if (reservationRepository.existsByTime_Id(id)) {
            throw new IllegalArgumentException("삭제할 수 없는 예약 시간입니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationTimeResponse> getReservationTimes() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<TimeConditionResponse> getTimesWithCondition(final TimeConditionRequest request) {
        List<Reservation> reservations = reservationRepository.findByDateAndTheme_Id(request.date(), request.themeId());
        List<ReservationTime> times = reservationTimeRepository.findAll();

        return times.stream()
                .map(time -> toTimeConditionResponse(time, reservations))
                .toList();
    }

    private TimeConditionResponse toTimeConditionResponse(ReservationTime time, List<Reservation> reservations) {
        boolean hasTime = reservations.stream()
                .anyMatch(reservation -> reservation.isSameTime(time));
        return new TimeConditionResponse(time.getId(), time.getStartAt(), hasTime);
    }
}
