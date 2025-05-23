package roomescape.application.reservation.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.command.dto.CreateReservationTimeCommand;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.infrastructure.error.exception.ReservationTimeException;

@Service
@Transactional
public class CreateReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public CreateReservationTimeService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public Long register(CreateReservationTimeCommand createCommand) {
        validateAlreadyExistsReservationTime(createCommand);
        ReservationTime reservationTime = new ReservationTime(createCommand.startAt());
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return savedReservationTime.getId();
    }

    private void validateAlreadyExistsReservationTime(CreateReservationTimeCommand createReservationTimeCommand) {
        if (reservationTimeRepository.existsByStartAt(createReservationTimeCommand.startAt())) {
            throw new ReservationTimeException("이미 존재하는 예약시간입니다.");
        }
    }
}
