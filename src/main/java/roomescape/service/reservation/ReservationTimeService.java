package roomescape.service.reservation;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationTime;
import roomescape.dto.reservation.AddReservationTimeDto;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.exception.reservation.InvalidReservationTimeException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservation.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationRepository reservationRepository,
                                  ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public Long addReservationTime(AddReservationTimeDto addReservationTimeDto) {
        ReservationTime reservationTime = addReservationTimeDto.toEntity();
        if (reservationTimeRepository.existsByTime(reservationTime.getTime())) {
            throw new InvalidReservationTimeException("중복된 예약시간입니다");
        }
        return reservationTimeRepository.add(reservationTime);
    }

    public void deleteReservationTime(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new InvalidReservationException("예약이 되어있는 시간은 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    public ReservationTime getReservationTimeById(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new InvalidReservationTimeException("존재하지 않는 예약 시간입니다."));
    }

    public List<ReservationTime> allReservationTimes() {
        return reservationTimeRepository.findAll();
    }
}
