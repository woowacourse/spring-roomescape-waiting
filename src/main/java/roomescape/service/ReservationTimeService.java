package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnprocessableException;
import roomescape.controller.dto.request.AvailableTimeFindRequest;
import roomescape.controller.dto.request.ReservationTimeCreateRequest;
import roomescape.domain.reservation.ReservationTime;
import roomescape.repository.JdbcSlotRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final JdbcSlotRepository slotRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  JdbcSlotRepository slotRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public ReservationTime create(ReservationTimeCreateRequest request) {
        ReservationTime reservationTime = ReservationTime.of(request.getStartAt());
        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public List<ReservationTime> findAvailable(AvailableTimeFindRequest request, LocalDate now) {
        if (now.isAfter(request.getDate())) {
            throw new UnprocessableException("기준 날짜는 과거일 수 없습니다. 오늘 이후 날짜를 입력해 주세요");
        }

        return reservationTimeRepository.findByDateAndTheme(request.getDate(), request.getThemeId());
    }

    @Transactional
    public void delete(long reservationTimeId) {
        if (!reservationTimeRepository.existsById(reservationTimeId)) {
            throw new NotFoundException("존재하지 않는 시간입니다. 입력을 확인해 주세요.");
        }

        if (slotRepository.existsByTimeId(reservationTimeId)) {
            throw new ConflictException("시간을 사용하는 예약이 존재합니다. 관련 예약을 지우고 요청해 주세요.");
        }

        reservationTimeRepository.delete(reservationTimeId);
    }
}
