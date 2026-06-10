package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.AvailableTimeFindRequest;
import roomescape.controller.dto.request.ReservationTimeCreateRequest;
import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.SlotRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {
    private final Clock clock;
    private final ReservationTimeRepository reservationTimeRepository;
    private final SlotRepository slotRepository;

    public ReservationTimeService(
            Clock clock,
            ReservationTimeRepository reservationTimeRepository,
            SlotRepository slotRepository
    ) {
        this.clock = clock;
        this.reservationTimeRepository = reservationTimeRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public ReservationTime create(ReservationTimeCreateRequest request) {
        ReservationTime reservationTime = ReservationTime.create(request.getStartAt());
        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public List<ReservationTime> findAvailable(AvailableTimeFindRequest request) {
        LocalDate now = LocalDate.now(clock);

        if (now.isAfter(request.getDate())) {
            throw new RoomEscapeException(DomainErrorCode.PAST_DATE, "지나간 날짜는 조회할 수 없습니다: " + request.getDate());
        }

        return reservationTimeRepository.findByDateAndTheme(request.getDate(), request.getThemeId());
    }

    @Transactional
    public void delete(long reservationTimeId) {
        if (!reservationTimeRepository.existsById(reservationTimeId)) {
            throw new RoomEscapeException(DomainErrorCode.RESOURCE_NOT_FOUND, "해당 예약 시간을 찾을 수 없습니다: " + reservationTimeId);
        }

        if (slotRepository.existsByTimeId(reservationTimeId)) {
            throw new RoomEscapeException(DomainErrorCode.RESOURCE_IN_USE, "해당 예약 시간은 사용 중이라 삭제할 수 없습니다: " + reservationTimeId);
        }

        reservationTimeRepository.delete(reservationTimeId);
    }
}
