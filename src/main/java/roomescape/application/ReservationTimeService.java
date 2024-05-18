package roomescape.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.AvailableTimeResponse;
import roomescape.application.dto.ReservationTimeRequest;
import roomescape.application.dto.ReservationTimeResponse;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.ReservationTimeCommandRepository;
import roomescape.domain.repository.ReservationTimeQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class ReservationTimeService {

    private final ReservationTimeCommandRepository reservationTimeCommandRepository;
    private final ReservationTimeQueryRepository reservationTimeQueryRepository;
    private final ReservationQueryRepository reservationQueryRepository;

    public ReservationTimeService(ReservationTimeCommandRepository reservationTimeCommandRepository,
                                  ReservationQueryRepository reservationQueryRepository,
                                  ReservationTimeQueryRepository reservationTimeQueryRepository) {
        this.reservationTimeCommandRepository = reservationTimeCommandRepository;
        this.reservationQueryRepository = reservationQueryRepository;
        this.reservationTimeQueryRepository = reservationTimeQueryRepository;
    }

    @Transactional
    public ReservationTimeResponse create(ReservationTimeRequest request) {
        LocalTime startAt = request.startAt();
        if (existsByStartAt(startAt)) {
            throw new RoomescapeException(RoomescapeErrorCode.DUPLICATED_TIME,
                    String.format("중복된 예약 시간입니다. 요청 예약 시간:%s", startAt));
        }

        ReservationTime reservationTime = reservationTimeCommandRepository.save(request.toReservationTime());
        return ReservationTimeResponse.from(reservationTime);
    }

    private boolean existsByStartAt(LocalTime startAt) {
        return reservationTimeQueryRepository.existsByStartAt(startAt);
    }

    @Transactional
    public void deleteById(Long id) {
        ReservationTime findReservationTime = reservationTimeQueryRepository.getById(id);
        long reservedCount = reservationQueryRepository.countByTimeId(id);
        if (reservedCount > 0) {
            throw new RoomescapeException(RoomescapeErrorCode.ALREADY_RESERVED,
                    String.format("해당 예약 시간에 연관된 예약이 존재하여 삭제할 수 없습니다. 삭제 요청한 시간:%s", findReservationTime.getStartAt()));
        }
        reservationTimeCommandRepository.deleteById(id);
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeQueryRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableTimeResponse> findAvailableTimes(LocalDate date, Long themeId) {
        return reservationQueryRepository.findAvailableReservationTimes(date, themeId)
                .stream()
                .map(AvailableTimeResponse::from)
                .toList();
    }
}
