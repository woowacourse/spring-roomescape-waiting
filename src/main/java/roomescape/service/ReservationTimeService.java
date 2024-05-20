package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.FindTimeAndAvailabilityDto;

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

    public ReservationTime save(String startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);
        validateDuplication(startAt);
        return reservationTimeRepository.save(reservationTime);
    }

    private void validateDuplication(String time) {
        if (reservationTimeRepository.existsByStartAt(LocalTime.parse(time))) {
            throw new RoomescapeException("이미 존재하는 시간은 추가할 수 없습니다.");
        }
    }

    public void delete(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new RoomescapeException("해당 시간을 사용하는 예약이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FindTimeAndAvailabilityDto> findAllWithBookAvailability(LocalDate date, Long themeId) {
        List<Reservation> reservations =
            reservationRepository.findAllByDateAndThemeId(date, themeId);

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
