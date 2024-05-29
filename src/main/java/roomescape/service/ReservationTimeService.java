package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationTimeDto;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository,
                                  WaitingRepository waitingRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    public ReservationTime saveReservationTime(ReservationTimeDto timeDto) {
        validateDuplication(timeDto);
        ReservationTime time = timeDto.toReservationTime();
        return reservationTimeRepository.save(time);
    }

    public ReservationTime findReservationTime(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[ERROR] 존재하지 않는 시간입니다."));
    }

    public void deleteReservationTime(long id) {
        validateExistence(id);
        validateDependenceOfReservation(id);
        validateDependenceOfWaiting(id);
        reservationTimeRepository.deleteById(id);
    }

    private void validateDuplication(ReservationTimeDto timeDto) {
        LocalTime startAt = timeDto.getStartAt();
        boolean isExist = reservationTimeRepository.existsByStartAt(startAt);
        if (isExist) {
            throw new DuplicatedException("[ERROR] 중복된 시간은 추가할 수 없습니다.");
        }
    }

    private void validateExistence(Long id) {
        boolean isNotExist = !reservationTimeRepository.existsById(id);
        if (isNotExist) {
            throw new NotFoundException("[ERROR] 존재하지 않는 시간입니다.");
        }
    }

    private void validateDependenceOfReservation(Long id) {
        boolean isExist = reservationRepository.existsByReservationInfo_TimeId(id);
        if (isExist) {
            throw new BadRequestException("[ERROR] 해당 시간을 사용하고 있는 예약이 있습니다.");
        }
    }

    private void validateDependenceOfWaiting(Long id) {
        boolean isExist = waitingRepository.existsByReservationInfo_TimeId(id);
        if (isExist) {
            throw new BadRequestException("[ERROR] 해당 시간을 사용하고 있는 예약 대기가 있습니다.");
        }
    }
}
