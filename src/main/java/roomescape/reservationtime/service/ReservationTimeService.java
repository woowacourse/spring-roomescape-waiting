package roomescape.reservationtime.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponseWithBookedStatus;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@Service
@AllArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeResponse create(ReservationTimeCreateRequest request) {
        ReservationTime reservationTime = new ReservationTime(null, request.startAt());
        return ReservationTimeResponse.fromReservationTime(
            reservationTimeRepository.save(reservationTime)
        );
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll().stream()
            .map(ReservationTimeResponse::fromReservationTime)
            .toList();
    }

    public void deleteById(Long id) {
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationTimeResponseWithBookedStatus> findAvailableReservationTimesByDateAndThemeId(
        LocalDate date,
        Long themeId
    ) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        List<ReservationTime> availableTimes = reservationTimeRepository.findAllByDateAndThemeId(date, themeId);

        return allTimes.stream()
            .map(time ->
                new ReservationTimeResponseWithBookedStatus(
                    time.getId(),
                    time.getStartAt(),
                    !availableTimes.contains(time)
                )
            ).toList();
    }

    public Optional<ReservationTime> findById(Long id) {
        return reservationTimeRepository.findById(id);
    }

    public ReservationTime findByIdOrThrow(Long id) {
        return reservationTimeRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("존재하지 않는 예약 시간입니다."));
    }

    public List<ReservationTime> findByReservationDateAndThemeId(LocalDate date, Long themeId) {
        return reservationTimeRepository.findAllByDateAndThemeId(date, themeId);
    }
}
