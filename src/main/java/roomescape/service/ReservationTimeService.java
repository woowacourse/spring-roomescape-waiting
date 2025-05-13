package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.persistence.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.persistence.ReservationTimeRepository;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundReservationTimeException;
import roomescape.service.param.CreateReservationTimeParam;
import roomescape.service.result.AvailableReservationTimeResult;
import roomescape.service.result.ReservationTimeResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, final ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public Long create(CreateReservationTimeParam createReservationTimeParam) {
        ReservationTime reservationTime = new ReservationTime(
                null,
                createReservationTimeParam.startAt()
        );

        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return savedReservationTime.getId();
    }

    public ReservationTimeResult findById(Long reservationTimeId) {
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationTimeId).orElseThrow(
                () -> new NotFoundReservationTimeException(reservationTimeId + "에 해당하는 reservation_time 튜플이 없습니다."));
        return toReservationResult(reservationTime);
    }

    public List<ReservationTimeResult> findAll() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(this::toReservationResult)
                .toList();
    }

    public List<AvailableReservationTimeResult> findAvailableTimesByThemeIdAndDate(Long themeId, LocalDate reservationDate) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        Set<ReservationTime> bookedTimes = reservationRepository.findByThemeIdAndDate(themeId, reservationDate).stream()
                .map(Reservation::getTime)
                .filter(reservationTimes::contains)
                .collect(Collectors.toSet());

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
            throw new DeletionNotAllowedException("해당 예약 시간에 예약이 존재합니다.");
        }
        reservationTimeRepository.deleteById(reservationTimeId);
    }

    private ReservationTimeResult toReservationResult(ReservationTime reservationTime) {
        return new ReservationTimeResult(reservationTime.getId(), reservationTime.getStartAt());
    }
}
