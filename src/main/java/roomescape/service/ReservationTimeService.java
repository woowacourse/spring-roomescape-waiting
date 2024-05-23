package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public ReservationTimeService(
        ReservationTimeRepository reservationTimeRepository,
        ReservationRepository reservationRepository
    ) {
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

    @Transactional
    public void delete(Long id) {
        ReservationTime reservationTime = reservationTimeRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException("존재하지 않는 예약 시간 id 입니다."));
        reservationTime.validateHavingReservations();

        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public List<FindTimeAndAvailabilityDto> findAllWithBookAvailability(LocalDate date, Long themeId) {
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
