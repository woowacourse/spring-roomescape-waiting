package roomescape.time.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.CreateReservationTimeRequest;
import roomescape.time.dto.ReservationTimeResponse;
import roomescape.time.repository.ReservationTimeRepository;

import java.util.List;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationTimeResponse createReservationTime(final CreateReservationTimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new IllegalArgumentException("이미 존재하는 시간입니다.");
        }
        final ReservationTime reservationTime = request.convertToReservationTime();
        final ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return new ReservationTimeResponse(savedReservationTime);
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> getReservationTimes() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    @Transactional
    public void deleteReservationTimeById(final long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new IllegalArgumentException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
