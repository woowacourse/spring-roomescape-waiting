package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationTimeResponse createTime(ReservationTimeCreateRequest request) {
        ReservationTime time = request.toEntity();
        validateOperatingTime(time);
        validateDuplicated(time);
        ReservationTime saved = reservationTimeRepository.save(time);
        return ReservationTimeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> getAllTimes() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailableReservationTimeResponse> getAvailableTimes(LocalDate date, Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<ReservationTime> reservedTimes = reservationTimeRepository.findAllReservedTimeByDateAndThemeId(date,
                themeId);
        return reservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTimeResponse(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        reservedTimes.contains(reservationTime)
                ))
                .toList();
    }

    @Transactional
    public void deleteTime(Long id) {
        validateExistReservedReservation(id);
        reservationTimeRepository.deleteById(id);
    }

    private void validateOperatingTime(ReservationTime reservationTime) {
        if (!reservationTime.isAvailable()) {
            throw new BadRequestException("운영 시간 이외의 날짜는 예약할 수 없습니다.");
        }
    }

    private void validateDuplicated(ReservationTime reservationTime) {
        List<ReservationTime> times = reservationTimeRepository.findAll();
        if (times.stream().anyMatch(time -> time.isDuplicatedWith(reservationTime))) {
            throw new ConflictException("러닝 타임이 겹치는 시간이 존재합니다.");
        }
    }

    private void validateExistReservedReservation(Long timeId) {
        if (reservationRepository.existsByTimeId(timeId)) {
            throw new BadRequestException("해당 시간에 예약된 내역이 존재하므로 삭제할 수 없습니다.");
        }
    }
}
