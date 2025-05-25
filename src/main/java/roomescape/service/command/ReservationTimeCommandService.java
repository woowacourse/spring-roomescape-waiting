package roomescape.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.dto.time.ReservationTimeCreateRequestDto;
import roomescape.dto.time.ReservationTimeResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;

@Service
@Transactional
public class ReservationTimeCommandService {

    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaReservationRepository reservationRepository;

    public ReservationTimeCommandService(JpaReservationTimeRepository reservationTimeRepository,
                                         JpaReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponseDto createReservationTime(final ReservationTimeCreateRequestDto requestDto) {
        ReservationTime requestTime = requestDto.createWithoutId();
        try {
            ReservationTime savedTime = reservationTimeRepository.save(requestTime);
            return new ReservationTimeResponseDto(savedTime);
        } catch (IllegalStateException e) {
            throw new DuplicateContentException(e.getMessage());
        }
    }

    public void deleteReservationTimeById(final Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new IllegalStateException("이 시간의 예약이 이미 존재합니다. id : " + id);
        }
        if (!reservationTimeRepository.existsById(id)) {
            throw new NotFoundException("등록된 시간만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }

        reservationTimeRepository.deleteById(id);
    }
}
