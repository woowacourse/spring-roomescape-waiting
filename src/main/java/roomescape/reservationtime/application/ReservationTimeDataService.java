package roomescape.reservationtime.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.exception.ReservationTimeDuplicatedException;
import roomescape.reservationtime.exception.ReservationTimeInUseException;
import roomescape.reservationtime.exception.ReservationTimeNotFoundException;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeWebResponse;

@Service
public class ReservationTimeDataService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationSlotDataService reservationSlotDataService;

    public ReservationTimeDataService(final ReservationTimeRepository reservationTimeRepository,
                                      final ReservationSlotDataService reservationSlotDataService) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationSlotDataService = reservationSlotDataService;
    }

    public ReservationTime create(final ReservationTime reservationTime) {
        validateDistinct(reservationTime.getStartAt());
        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public ReservationTime getById(final Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new ReservationTimeNotFoundException("요청한 id와 일치하는 예약 시간 정보가 없습니다."));
    }

    public List<AvailableReservationTimeWebResponse> findAvailable(
            final LocalDate date,
            final Long themeId
    ) {
        return reservationTimeRepository.findAvailable(date, themeId);
    }

    public void delete(Long id) {
        validateExist(id);
        reservationTimeRepository.deleteById(id);
    }

    private void validateExist(final Long id) {
        if (reservationSlotDataService.existsByTimeId(id)) {
            throw new ReservationTimeInUseException("해당 시간에 대한 예약이 존재하여 삭제할 수 없습니다.");
        }
    }

    private void validateDistinct(final LocalTime reservationTime) {
        if (reservationTimeRepository.existsByStartAt(reservationTime)) {
            throw new ReservationTimeDuplicatedException("중복된 예약 시간을 생성할 수 없습니다.");
        }
    }
}
