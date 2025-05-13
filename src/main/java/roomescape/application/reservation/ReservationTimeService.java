package roomescape.application.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import roomescape.application.reservation.dto.AvailableReservationTimeResult;
import roomescape.application.reservation.dto.CreateReservationTimeParam;
import roomescape.application.reservation.dto.ReservationTimeResult;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.reservation.DailyThemeReservations;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTImeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTImeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTImeRepository = reservationTImeRepository;
        this.reservationRepository = reservationRepository;
    }

    public Long create(CreateReservationTimeParam createReservationTimeParam) {
        ReservationTime reservationTime = reservationTImeRepository.save(
                new ReservationTime(createReservationTimeParam.startAt())
        );
        return reservationTime.getId();
    }

    public ReservationTimeResult findById(Long reservationTimeId) {
        ReservationTime reservationTime = reservationTImeRepository.findById(reservationTimeId)
                .orElseThrow(
                        () -> new NotFoundEntityException(reservationTimeId + "에 해당하는 reservation_time 튜플이 없습니다."));
        return toReservationResult(reservationTime);
    }

    public List<ReservationTimeResult> findAll() {
        List<ReservationTime> reservationTimes = reservationTImeRepository.findAll();
        return reservationTimes.stream()
                .map(this::toReservationResult)
                .toList();
    }

    public List<AvailableReservationTimeResult> findAvailableTimesByThemeIdAndDate(Long themeId,
                                                                                   LocalDate reservationDate) {
        List<ReservationTime> reservationTimes = reservationTImeRepository.findAll();
        List<Reservation> reservations = reservationRepository.findByThemeIdAndDate(
                themeId,
                reservationDate
        );
        DailyThemeReservations dailyThemeReservations = new DailyThemeReservations(
                reservations,
                themeId,
                reservationDate
        );
        Set<ReservationTime> bookedTimes = dailyThemeReservations.calculateBookedTimes();
        return reservationTimes.stream()
                .map(reservationTime ->
                        new AvailableReservationTimeResult(
                                reservationTime.getId(),
                                reservationTime.getStartAt(),
                                bookedTimes.contains(reservationTime)
                        )
                )
                .toList();
    }

    public void deleteById(Long reservationTimeId) {
        if (reservationRepository.existsByTimeId(reservationTimeId)) {
            throw new BusinessRuleViolationException("해당 예약 시간에 예약이 존재합니다.");
        }
        reservationTImeRepository.deleteById(reservationTimeId);
    }

    private ReservationTimeResult toReservationResult(ReservationTime reservationTime) {
        return new ReservationTimeResult(reservationTime.getId(), reservationTime.getStartAt());
    }
}
