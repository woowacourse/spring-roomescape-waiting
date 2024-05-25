package roomescape.domain.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.reservation.dto.command.ReservationTimeAddCommand;
import roomescape.domain.reservation.repository.reservationTime.ReservationTimeRepository;
import roomescape.global.exception.EscapeApplicationException;
import roomescape.global.exception.NoMatchingDataException;

@Service
public class AdminReservationTimeService {

    protected static final String DUPLICATED_RESERVATION_TIME_ERROR_MESSAGE = "이미 존재하는 예약시간은 추가할 수 없습니다.";
    protected static final String NON_EXIST_RESERVATION_TIME_ID_ERROR_MESSAGE = "해당 id를 가진 예약시간이 존재하지 않습니다.";

    private final ReservationTimeRepository reservationTimeRepository;

    public AdminReservationTimeService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTime> findAllReservationTime() {
        return reservationTimeRepository.findAll();
    }

    public ReservationTime addReservationTime(ReservationTimeAddCommand reservationTimeAddCommand) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeAddCommand.startAt())) {
            throw new EscapeApplicationException(DUPLICATED_RESERVATION_TIME_ERROR_MESSAGE);
        }
        ReservationTime reservationTime = reservationTimeAddCommand.toEntity();
        return reservationTimeRepository.save(reservationTime);
    }

    public void removeReservationTime(Long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new NoMatchingDataException(NON_EXIST_RESERVATION_TIME_ID_ERROR_MESSAGE);
        }
        reservationTimeRepository.deleteById(id);
    }
}
