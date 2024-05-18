package roomescape.domain.reservation.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.reservation.dto.ReservationTimeAddRequest;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.global.exception.EscapeApplicationException;

import java.util.List;

@Service
public class AdminReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public AdminReservationTimeService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTime> findAllReservationTime() {
        return reservationTimeRepository.findAll();
    }

    public ReservationTime addReservationTime(ReservationTimeAddRequest reservationTimeAddRequest) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeAddRequest.startAt())) {
            throw new EscapeApplicationException("이미 존재하는 예약시간은 추가할 수 없습니다.");
        }

        return reservationTimeRepository.save(reservationTimeAddRequest.toEntity());
    }

    public void removeReservationTime(Long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("해당 id를 가진 예약시간이 존재하지 않습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
