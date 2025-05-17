package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;

import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.dto.AvailableTimeInfo;
import roomescape.reservation.service.dto.ReservationTimeCreateCommand;
import roomescape.reservation.service.dto.ReservationTimeInfo;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeInfo createReservationTime(final ReservationTimeCreateCommand command) {
        if (reservationTimeRepository.existsByTime(command.startAt())) {
            throw new IllegalArgumentException("이미 존재하는 시간입니다.");
        }
        final ReservationTime reservationTime = command.convertToReservationTime();
        final ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return new ReservationTimeInfo(savedReservationTime);
    }

    public List<ReservationTimeInfo> getReservationTimes() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeInfo::new)
                .toList();
    }

    public void deleteReservationTimeById(final long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new IllegalArgumentException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<AvailableTimeInfo> findAvailableTimes(final LocalDate date, final long themeId) {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final List<Reservation> reservations = reservationRepository.findAllByDateAndThemeId(date, themeId);
        return reservationTimes.stream()
                .map(time ->
                        new AvailableTimeInfo(time.getId(), time.getStartAt(), isAlreadyBooked(time, reservations)))
                .toList();
    }

    private boolean isAlreadyBooked(final ReservationTime reservationTime, final List<Reservation> reservations) {
        return reservations.stream().anyMatch(reservation -> reservation.isSameTime(reservationTime));
    }
}
