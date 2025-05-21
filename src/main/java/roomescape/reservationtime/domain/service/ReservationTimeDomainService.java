package roomescape.reservationtime.domain.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.domain.service.ReservationDomainService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.exception.ReservationTimeAlreadyExistsException;
import roomescape.reservationtime.exception.ReservationTimeInUseException;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;

@Service
public class ReservationTimeDomainService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationDomainService reservationDomainService;

    public ReservationTimeDomainService(final ReservationTimeRepository reservationTimeRepository,
                                        final ReservationDomainService reservationDomainService) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationDomainService = reservationDomainService;
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public void delete(Long id) {
        if (reservationDomainService.existsByTimeId(id)) {
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
                .orElseThrow(() -> new ReservationNotFoundException("요청한 id와 일치하는 예약 시간 정보가 없습니다."));
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
