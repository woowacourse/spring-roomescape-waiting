package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.dto.SaveReservationTimeRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTime> getReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    public ReservationTime saveReservationTime(final SaveReservationTimeRequest request) {
        validateReservationTimeDuplication(request);

        return reservationTimeRepository.save(request.toReservationTime());
    }

    private void validateReservationTimeDuplication(final SaveReservationTimeRequest request) {
        if (reservationTimeRepository.existByStartAt(request.startAt())) {
            throw new IllegalArgumentException("이미 존재하는 예약시간이 있습니다.");
        }
    }

    public void deleteReservationTime(final Long reservationTimeId) {
        validateReservationTimeExist(reservationTimeId);
        final int deletedDataCount = reservationTimeRepository.deleteById(reservationTimeId);

        if (deletedDataCount <= 0) {
            throw new NoSuchElementException("해당 id의 예약 시간이 존재하지 않습니다.");
        }
    }

    private void validateReservationTimeExist(final Long reservationTimeId) {
        if (reservationRepository.existByTimeId(reservationTimeId)) {
            throw new IllegalArgumentException("예약에 포함된 시간 정보는 삭제할 수 없습니다.");
        }
    }
}
