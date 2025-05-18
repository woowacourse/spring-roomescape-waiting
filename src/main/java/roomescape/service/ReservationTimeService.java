package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimePolicy;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundReservationTimeException;
import roomescape.persistence.dto.ReservationTimeAvailabilityData;
import roomescape.service.param.CreateReservationTimeParam;
import roomescape.service.result.AvailableReservationTimeResult;
import roomescape.service.result.ReservationTimeResult;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimePolicy reservationTimePolicy;

    public ReservationTimeService(final ReservationTimeRepository reservationTimeRepository,
                                  final ReservationRepository reservationRepository,
                                  ReservationTimePolicy reservationTimePolicy) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimePolicy = reservationTimePolicy;
    }

    public ReservationTimeResult create(CreateReservationTimeParam createReservationTimeParam) {
        LocalTime startAt = createReservationTimeParam.startAt();
        reservationTimePolicy.validate(startAt);

        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTime.createNew(startAt));
        return ReservationTimeResult.from(reservationTime);
    }

    public ReservationTimeResult getById(Long reservationTimeId) {
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationTimeId).orElseThrow(
                () -> new NotFoundReservationTimeException(reservationTimeId + "에 해당하는 reservation_time 튜플이 없습니다."));
        return toReservationResult(reservationTime);
    }

    public List<ReservationTimeResult> getAll() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(this::toReservationResult)
                .toList();
    }

    public List<AvailableReservationTimeResult> getAvailableTimesByThemeIdAndDate(Long themeId,
                                                                                  LocalDate reservationDate) {
        List<ReservationTimeAvailabilityData> availableTimesData = reservationTimeRepository.findAvailableTimesByThemeAndDate(
                themeId, reservationDate);

        return availableTimesData.stream()
                .map(data -> new AvailableReservationTimeResult(
                        data.id(), data.startAt(), data.booked()
                )).toList();
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
