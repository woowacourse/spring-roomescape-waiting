package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Date;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Time;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.FindTimeAndAvailabilityDto;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
        ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTime save(String startAt) {
        ReservationTime newReservationTime = new ReservationTime(startAt);
        validateDuplication(newReservationTime.getStartAt());

        return reservationTimeRepository.save(newReservationTime);
    }

    private void validateDuplication(LocalTime parsedTime) {
        if (reservationTimeRepository.existsByStartAt(new Time(parsedTime.toString()))) { // TODO: 인자 구조 개선
            throw new RoomescapeException("이미 존재하는 시간은 추가할 수 없습니다.");
        }
    }

    public void delete(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new RoomescapeException("해당 시간을 사용하는 예약이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public List<FindTimeAndAvailabilityDto> findAllWithBookAvailability(LocalDate date, Long themeId) {
        // TODO: 인자 구조 개선
        List<Reservation> reservations = reservationRepository.findAllByDateAndThemeId(new Date(date.toString()),
            themeId);
        List<ReservationTime> reservedTimes = reservations.stream()
            .map(Reservation::getTime)
            .toList();

        return findAll().stream()
            .map(time -> new FindTimeAndAvailabilityDto(
                    time.getId(),
                    time.getStartAt(),
                    reservedTimes.contains(time)
                )
            ).toList();
    }
}
