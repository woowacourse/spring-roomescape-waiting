package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.dto.response.ReservationTimeCreateResponse;
import roomescape.reservation.dto.response.ReservationTimeReadResponse;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeCreateResponse createTime(ReservationTimeCreateRequest request) {
        ReservationTime time = request.toEntity();
        validateOperatingTime(time);
        validateDuplicated(time);
        ReservationTime saved = reservationTimeRepository.save(time);
        return ReservationTimeCreateResponse.from(saved);
    }

    public List<ReservationTimeReadResponse> getAllTimes() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeReadResponse::from)
                .toList();
    }

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

    public void deleteTime(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new BadRequestException("해당 시간에 예약된 내역이 존재하므로 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    private void validateOperatingTime(ReservationTime entity) {
        if (!entity.isAvailable()) {
            throw new BadRequestException("운영 시간 이외의 날짜는 예약할 수 없습니다.");
        }
    }

    private void validateDuplicated(ReservationTime entity) {
        List<ReservationTime> times = reservationTimeRepository.findAll();
        if (times.stream().anyMatch(time -> time.isDuplicatedWith(entity))) {
            throw new ConflictException("러닝 타임이 겹치는 시간이 존재합니다.");
        }
    }
}
