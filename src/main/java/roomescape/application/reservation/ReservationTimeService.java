package roomescape.application.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.AvailableReservationTimeResult;
import roomescape.application.reservation.dto.CreateReservationTimeParam;
import roomescape.application.reservation.dto.ReservationTimeResult;
import roomescape.infrastructure.error.exception.ReservationTimeException;
import roomescape.domain.reservation.DailyThemeReservations;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;

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
    public Long create(CreateReservationTimeParam createReservationTimeParam) {
        if (reservationTimeRepository.existsByStartAt(createReservationTimeParam.startAt())) {
            throw new ReservationTimeException("이미 존재하는 얘약시간입니다.");
        }
        ReservationTime reservationTime = reservationTimeRepository.save(
                new ReservationTime(createReservationTimeParam.startAt())
        );
        return reservationTime.getId();
    }

    public ReservationTimeResult findById(Long reservationTimeId) {
        ReservationTime reservationTime = getReservationTimeById(reservationTimeId);
        return toReservationResult(reservationTime);
    }

    private ReservationTime getReservationTimeById(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeException(timeId + "에 해당하는 reservation_time 튜플이 없습니다."));
    }

    public List<ReservationTimeResult> findAll() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(this::toReservationResult)
                .toList();
    }

    public List<AvailableReservationTimeResult> findAvailableTimesByThemeIdAndDate(Long themeId,
                                                                                   LocalDate reservationDate) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
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

    @Transactional
    public void deleteById(Long reservationTimeId) {
        if (reservationRepository.existsByTimeId(reservationTimeId)) {
            throw new ReservationTimeException("해당 예약 시간에 예약이 존재합니다.");
        }
        reservationTimeRepository.deleteById(reservationTimeId);
    }

    private ReservationTimeResult toReservationResult(ReservationTime reservationTime) {
        return new ReservationTimeResult(reservationTime.getId(), reservationTime.getStartAt());
    }
}
