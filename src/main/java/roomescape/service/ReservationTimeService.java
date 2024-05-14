package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeStatuses;
import roomescape.repository.JdbcReservationRepository;
import roomescape.repository.JdbcReservationTimeRepository;
import roomescape.service.dto.reservation.ReservationTimeRequest;
import roomescape.service.dto.reservation.ReservationTimeResponse;
import roomescape.service.dto.time.AvailableTimeRequest;
import roomescape.service.dto.time.AvailableTimeResponses;

@Service
public class ReservationTimeService {

    private final JdbcReservationTimeRepository reservationTimeRepository;
    private final JdbcReservationRepository reservationRepository;

    public ReservationTimeService(JdbcReservationTimeRepository reservationTimeRepository,
                                  JdbcReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResponse> findAllReservationTimes() {
        return reservationTimeRepository.findAllReservationTimes()
                .stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public AvailableTimeResponses findAvailableReservationTimes(AvailableTimeRequest request) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAllReservationTimes();
        List<ReservationTime> bookedTimes = reservationTimeRepository.findReservedTimeByThemeAndDate(
                request.getDate(), request.getThemeId());

        ReservationTimeStatuses reservationStatuses = new ReservationTimeStatuses(allTimes, bookedTimes);
        return new AvailableTimeResponses(reservationStatuses);
    }

    public ReservationTimeResponse createReservationTime(ReservationTimeRequest request) {
        ReservationTime reservationTime = request.toReservationTime();
        if (reservationTimeRepository.isTimeExistsByStartTime(reservationTime.getStartAt().toString())) {
            throw new IllegalArgumentException("중복된 시간을 입력할 수 없습니다.");
        }
        ReservationTime savedTime = reservationTimeRepository.insertReservationTime(reservationTime);
        return new ReservationTimeResponse(savedTime);
    }

    public void deleteReservationTime(long id) {
        if (!reservationTimeRepository.isTimeExistsByTimeId(id)) {
            throw new IllegalArgumentException("존재하지 않는 아이디입니다.");
        }
        if (reservationRepository.isReservationExistsByTimeId(id)) {
            throw new IllegalArgumentException("해당 시간에 예약이 있어 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteReservationTimeById(id);
    }
}
