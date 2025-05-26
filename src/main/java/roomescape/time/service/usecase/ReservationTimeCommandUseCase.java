package roomescape.time.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.AlreadyExistException;
import roomescape.common.exception.ReferencedByOtherException;
import roomescape.reservation.service.usecase.ReservationQueryUseCase;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.time.service.converter.ReservationTimeConverter;
import roomescape.time.service.dto.CreateReservationTimeServiceRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationTimeCommandUseCase {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationQueryUseCase reservationQueryUseCase;
    private final ReservationTimeQueryUseCase reservationTimeQueryUseCase;

    public ReservationTime create(final CreateReservationTimeServiceRequest createReservationTimeServiceRequest) {
        if (reservationTimeQueryUseCase.existsByStartAt(createReservationTimeServiceRequest.startAt())) {
            throw new AlreadyExistException("추가하려는 시간이 이미 존재합니다.");
        }
        return reservationTimeRepository.save(
                ReservationTimeConverter.toDomain(createReservationTimeServiceRequest));
    }

    public void delete(final Long id) {
        if (reservationQueryUseCase.existsByTimeId(id)) {
            throw new ReferencedByOtherException("예약에서 참조 중인 시간은 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
