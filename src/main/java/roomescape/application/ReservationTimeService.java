package roomescape.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.AvailableTimeResponse;
import roomescape.application.dto.ReservationTimeRequest;
import roomescape.application.dto.ReservationTimeResponse;
import roomescape.domain.ReservationQueryRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimesRepository;
    private final ReservationQueryRepository reservationQueryRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimesRepository,
                                  ReservationQueryRepository reservationQueryRepository) {
        this.reservationTimesRepository = reservationTimesRepository;
        this.reservationQueryRepository = reservationQueryRepository;
    }

    @Transactional
    public ReservationTimeResponse create(ReservationTimeRequest request) {
        LocalTime startAt = request.startAt();
        if (existsByStartAt(startAt)) {
            throw new RoomescapeException(RoomescapeErrorCode.DUPLICATED_TIME,
                    String.format("중복된 예약 시간입니다. 요청 예약 시간:%s", startAt));
        }

        ReservationTime reservationTime = reservationTimesRepository.create(request.toReservationTime());
        return ReservationTimeResponse.from(reservationTime);
    }

    private boolean existsByStartAt(LocalTime startAt) {
        return reservationTimesRepository.existsByStartAt(startAt);
    }

    @Transactional
    public void deleteById(long id) {
        ReservationTime findReservationTime = reservationTimesRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_TIME));
        long reservedCount = reservationQueryRepository.findReservationCountByTimeId(id);
        if (reservedCount > 0) {
            throw new RoomescapeException(RoomescapeErrorCode.ALREADY_RESERVED,
                    String.format("해당 예약 시간에 연관된 예약이 존재하여 삭제할 수 없습니다. 삭제 요청한 시간:%s", findReservationTime.getStartAt()));
        }
        reservationTimesRepository.deleteById(id);
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimesRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableTimeResponse> findAvailableTimes(LocalDate date, long themeId) {
        return reservationQueryRepository.findAvailableReservationTimes(date, themeId)
                .stream()
                .map(AvailableTimeResponse::from)
                .toList();
    }
}
