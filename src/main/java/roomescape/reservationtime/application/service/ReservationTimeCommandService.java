package roomescape.reservationtime.application.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.RoomEscapeException;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.application.dto.ReservationTimeCreateCommand;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationTimeCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;

    public ReservationTimeResult save(ReservationTimeCreateCommand request) {
        validateDuplicateTime(request.startAt());
        ReservationTime time = request.toEntity();

        ReservationTime savedTime = timeRepository.save(time);

        return ReservationTimeResult.from(savedTime);
    }

    public void delete(Long id) {
        if (reservationRepository.existsByTime(id)) {
            throw new RoomEscapeException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }

        if (timeRepository.delete(id) == 0) {
            throw new NotFoundException("존재하지 않는 시간입니다.");
        }
    }
    
    private void validateDuplicateTime(LocalTime startAt) {
        if (timeRepository.existsByStartAt(startAt)) {
            throw new ConflictException(String.format("시간 %s이(가) 이미 존재합니다.",
                    startAt.format(DateTimeFormatter.ofPattern("HH:mm"))));
        }
    }
}
