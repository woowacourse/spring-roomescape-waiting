package roomescape.reservationtime.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservationslot.exception.ReservationSlotNotFoundException;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.exception.ReservationTimeAlreadyExistsException;
import roomescape.reservationtime.exception.ReservationTimeInUseException;

@Service
public class ReservationTimeDataService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationSlotDataService reservationSlotDataService;

    public ReservationTimeDataService(final ReservationTimeRepository reservationTimeRepository,
                                      final ReservationSlotDataService reservationSlotDataService) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationSlotDataService = reservationSlotDataService;
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public void delete(Long id) {
        if (reservationSlotDataService.existsByTimeId(id)) {
            throw new ReservationTimeInUseException("해당 시간에 대한 예약이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    public ReservationTime create(final ReservationTimeCreateRequest request) {
        validateIsTimeUnique(request);
        return reservationTimeRepository.save(request.toReservationTime());
    }

    public ReservationTime findReservationTime(final Long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new ReservationSlotNotFoundException("요청한 id와 일치하는 예약 시간 정보가 없습니다."));
    }

    public ReservationTime save(final ReservationTime reservationTime) {
        if (reservationTimeRepository.existsByStartAt(reservationTime.getStartAt())) {
            throw new ReservationTimeAlreadyExistsException("중복된 예약 시간을 생성할 수 없습니다.");
        }
        return reservationTimeRepository.save(reservationTime);
    }

    private void validateIsTimeUnique(final ReservationTimeCreateRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new ReservationTimeAlreadyExistsException("중복된 예약 시간을 생성할 수 없습니다.");
        }
    }
}
