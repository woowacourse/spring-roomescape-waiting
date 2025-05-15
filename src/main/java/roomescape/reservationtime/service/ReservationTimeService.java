package roomescape.reservationtime.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.ConstrainedDataException;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponse createReservationTime(final ReservationTimeCreateRequest requestDto) {
        if (reservationTimeRepository.existsByStartAt(requestDto.startAt())) {
            throw new DuplicateContentException("[ERROR] 이미 동일한 예약 시간이 존재합니다.");
        }
        ReservationTime requestTime = requestDto.createWithoutId();
        ReservationTime savedTime = reservationTimeRepository.save(requestTime);
        return ReservationTimeResponse.from(savedTime);
    }

    public List<ReservationTimeResponse> findAllReservationTimes() {
        List<ReservationTime> allReservationTime = reservationTimeRepository.findAll();
        return allReservationTime.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(LocalDate date, Long themeId) {
        List<ReservationTime> allReservationTimes = reservationTimeRepository.findAll();
        List<Reservation> reservationsOnDate = reservationRepository.findByDateAndThemeId(date, themeId);

        List<ReservationTime> reservedTimes = reservationsOnDate.stream()
                .map(Reservation::getTime)
                .toList();

        return allReservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTimeResponse(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        reservedTimes.contains(reservationTime)
                ))
                .toList();
    }

    public void deleteReservationTimeById(final Long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new NotFoundException("[ERROR] 등록된 예약 시간 번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }
        if (reservationRepository.findByTimeId(id).isPresent()) {
            throw new ConstrainedDataException("[ERROR] 해당 시간에 예약 기록이 존재합니다. 예약을 먼저 삭제해 주세요.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
