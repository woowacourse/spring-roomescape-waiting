package roomescape.service;

import java.time.LocalTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.ResourceInUseException;
import roomescape.dao.ReservationTimeDao;
import roomescape.dto.request.ReservationTimeRegisterDto;
import roomescape.dto.response.AvailableReservationTimeResponseDto;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.model.AvailableReservationTime;
import roomescape.model.ReservationTime;

@Service
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao) {
        this.reservationTimeDao = reservationTimeDao;
    }

    public List<ReservationTimeResponseDto> getAllTimes() {
        List<ReservationTime> reservationTimes = reservationTimeDao.findAll();

        return reservationTimes.stream()
                .map(ReservationTimeResponseDto::from)
                .toList();
    }

    public ReservationTimeResponseDto saveTime(ReservationTimeRegisterDto reservationTimeRegisterDto) {
        validateReservationTime(reservationTimeRegisterDto);

        ReservationTime reservationTime = reservationTimeRegisterDto.convertToTime();
        Long id = reservationTimeDao.saveTime(reservationTime);

        return new ReservationTimeResponseDto(id, reservationTime.getStartAt());
    }

    public void deleteTime(final Long id) {
        try {
            reservationTimeDao.deleteTimeById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("삭제하고자 하는 시각에 예약된 정보가 있습니다.");
        }
    }

    public List<AvailableReservationTimeResponseDto> getAvailableTimes(String date, Long themeId) {
        List<AvailableReservationTime> availableTimes = reservationTimeDao.findAvailableTimes(date, themeId);
        return availableTimes.stream().map(AvailableReservationTimeResponseDto::from).toList();
    }

    private void validateReservationTime(ReservationTimeRegisterDto reservationTimeRegisterDto) {
        LocalTime parsedStartAt = LocalTime.parse(reservationTimeRegisterDto.startAt());

        boolean duplicatedStartAtExisted = reservationTimeDao.isDuplicatedStartAtExisted(parsedStartAt);
        if (duplicatedStartAtExisted) {
            throw new DuplicatedException("중복된 예약시각은 등록할 수 없습니다.");
        }
    }
}
