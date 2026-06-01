package roomescape.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservationTimeRequestDTO;
import roomescape.dto.ReservationTimeResponseDTO;
import roomescape.exception.ReservationTimeErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

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
    public ReservationTimeResponseDTO addReservationTime(
            ReservationTimeRequestDTO reservationTimeRequest) {
        ReservationTime reservationTime = ReservationTime.create(reservationTimeRequest.startAt());

        validateDuplicateReservationTime(reservationTime);
        ReservationTime savedTime = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponseDTO.from(savedTime);
    }

    private void validateDuplicateReservationTime(ReservationTime reservationTime) {
        if (reservationTimeRepository.existByStartAt(reservationTime.getStartAt())) {
            throw new RoomEscapeException(ReservationTimeErrorCode.RESERVATION_TIME_DUPLICATE);
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponseDTO> findAllReservationTime() {
        return reservationTimeRepository.findAll().stream().map(ReservationTimeResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReservationTime(Long id) {
        validateReservationTimeExists(id);
        validateRemovableReservationTime(id);
        reservationTimeRepository.delete(id);
    }

    private void validateReservationTimeExists(Long id) {
        reservationTimeRepository.findById(id).orElseThrow(
                () -> new RoomEscapeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND)
        );
    }

    private void validateRemovableReservationTime(Long id) {
        if (reservationRepository.existByTimeId(id)) {
            throw new RoomEscapeException(ReservationTimeErrorCode.RESERVATION_EXIST_ON_TIME);
        }
    }
}
