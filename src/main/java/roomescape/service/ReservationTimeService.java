package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.CreateTimeResponse;
import roomescape.controller.dto.FindTimeAndAvailabilityResponse;
import roomescape.controller.dto.FindTimeResponse;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
        ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public CreateTimeResponse save(String startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);
        validateDuplication(startAt);
        return CreateTimeResponse.from(reservationTimeRepository.save(reservationTime));
    }

    private void validateDuplication(String time) {
        if (reservationTimeRepository.existsByStartAt(LocalTime.parse(time))) {
            throw new RoomescapeException("이미 존재하는 시간은 추가할 수 없습니다.");
        }
    }

    @Transactional
    public void delete(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new RoomescapeException("해당 시간을 사용하는 예약이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<FindTimeResponse> findAll() {
        List<ReservationTime> times = reservationTimeRepository.findAllByOrderByStartAtAsc();
        return times.stream()
            .map(FindTimeResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FindTimeAndAvailabilityResponse> findAllWithBookAvailability(LocalDate date, Long themeId) {
        List<ReservationTime> times = reservationTimeRepository.findAll();

        List<Reservation> reservations =
            reservationRepository.findAllByDateAndThemeIdOrderByTimeStartAtAsc(date, themeId);

        List<ReservationTime> reservedTimes = reservations.stream()
            .map(Reservation::getTime)
            .toList();

        return times.stream()
            .map(time -> new FindTimeAndAvailabilityResponse(
                time.getId(),
                time.getStartAt(),
                reservedTimes.contains(time)
            )).toList();
    }
}
