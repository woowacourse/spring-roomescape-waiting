package roomescape.service.reservationtime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.InvalidRequestException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeDeleteService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeDeleteService(ReservationTimeRepository reservationTimeRepository,
                                        ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void deleteReservationTime(long id) {
        reservationTimeRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 예약 시간 아이디 입니다."));

        if (reservationRepository.existsByTimeId(id)) {
            throw new InvalidRequestException("이미 예약중인 시간은 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
