package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Date;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Time;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.FindTimeAndAvailabilityDto;
import roomescape.system.exception.RoomescapeException;

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
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAllByStartAt(
            new Time(startAt));
        ReservationTime reservationTime = new ReservationTime(startAt);
        reservationTime.validateDuplication(reservationTimes);

        return reservationTimeRepository.save(reservationTime);
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

    public List<FindTimeAndAvailabilityDto> findAllWithBookAvailability(LocalDate date,
        Long themeId) {
        List<Reservation> reservations =
            reservationRepository.findAllByDateAndThemeId(new Date(date.toString()), themeId);

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
