package roomescape.reservationTime.service;

import org.springframework.stereotype.Service;
import roomescape.common.exception.BusinessException;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.presentation.dto.ReservationTimeRequest;

@Service
public class ReservationTimeCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeCommandService(ReservationRepository reservationRepository,
                                         ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTime save(final ReservationTimeRequest request) {
        return reservationTimeRepository.save(new ReservationTime(request.startAt()));
    }

    public void deleteById(final Long id) {
        validateExistsIdToDelete(id);
        validateExistsTime(id);

        reservationTimeRepository.deleteById(id);
    }

    private void validateExistsTime(Long id) {
        if (!reservationTimeRepository.existsById(id)) {
            throw new BusinessException("해당 시간은 존재하지 않습니다.");
        }
    }

    private void validateExistsIdToDelete(final Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new BusinessException("해당 시간에 예약이 존재해서 삭제할 수 없습니다.");
        }
    }
}
