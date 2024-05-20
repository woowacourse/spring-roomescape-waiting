package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.ViolationException;
import roomescape.reservation.domain.*;
import roomescape.reservation.dto.response.AvailableReservationTimeResponse;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationTime create(ReservationTime reservationTime) {
        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public ReservationTime findById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 ID의 예약 시간이 없습니다."));
    }

    @Transactional
    public void delete(Long id) {
        reservationTimeRepository.findById(id)
                .ifPresent(this::validateHasReservation);
        reservationTimeRepository.deleteById(id);
    }

    private void validateHasReservation(ReservationTime reservationTime) {
        int reservationCount = reservationRepository.countByTime(reservationTime);
        if (reservationCount > 0) {
            throw new ViolationException("해당 예약 시간의 예약 건이 존재합니다.");
        }
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(LocalDate date, Theme theme) {
        final List<Reservation> reservations = reservationRepository.findAllByDateAndTheme(date, theme);
        Set<Long> reservedTimeIds = new HashSet<>(toTimeIds(reservations));
        List<ReservationTime> times = reservationTimeRepository.findAll();

        return times.stream()
                .map(reservationTime -> {
                    boolean isReserved = reservedTimeIds.contains(reservationTime.getId());
                    return AvailableReservationTimeResponse.of(reservationTime, isReserved);
                })
                .collect(Collectors.toList());
    }

    private List<Long> toTimeIds(final List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getTimeId)
                .toList();
    }
}
