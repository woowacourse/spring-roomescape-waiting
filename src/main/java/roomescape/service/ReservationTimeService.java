package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.ReservationTime;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.ReservationTimeDto;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeRepository.findAllReservationTimes();
    }

    public ReservationTime saveReservationTime(ReservationTimeDto timeDto) {
        validateDuplication(timeDto.getStartAt());
        ReservationTime time = ReservationTime.from(timeDto);
        Optional<ReservationTime> savedTime = reservationTimeRepository.saveReservationTime(time);
        return savedTime.orElseThrow(() -> new BadRequestException("[ERROR] 데이터가 저장되지 않았습니다."));
    }

    public ReservationTime findReservationTime(long id) {
        validateExistence(id);
        Optional<ReservationTime> time = reservationTimeRepository.findReservationById(id);
        return time.orElseThrow(() -> new NotFoundException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    public void deleteReservationTime(long id) {
        validateExistence(id);
        validateDependence(id);
        reservationTimeRepository.deleteReservationTimeById(id);
    }

    private void validateDuplication(LocalTime startAt) {
        boolean isExist = reservationTimeRepository.isExistReservationTimeByStartAt(startAt);
        if (isExist) {
            throw new DuplicatedException("[ERROR] 중복된 시간은 추가할 수 없습니다.");
        }
    }

    private void validateExistence(Long id) {
        boolean isNotExist = !reservationTimeRepository.isExistReservationTimeById(id);
        if (isNotExist) {
            throw new NotFoundException("[ERROR] 존재하지 않는 시간입니다.");
        }
    }

    private void validateDependence(Long id) {
        boolean isExist = reservationTimeRepository.isExistReservationByTimeId(id);
        if (isExist) {
            throw new BadRequestException("[ERROR] 해당 시간을 사용하고 있는 예약이 있습니다.");
        }
    }
}
