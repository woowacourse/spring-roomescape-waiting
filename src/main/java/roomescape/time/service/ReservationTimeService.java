package roomescape.time.service;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.controller.request.AvailableReservationTimeRequest;
import roomescape.time.controller.request.ReservationTimeCreateRequest;
import roomescape.time.controller.response.AvailableReservationTimeResponse;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponse open(ReservationTimeCreateRequest request) {
        LocalTime startAt = request.startAt();
        isAlreadyOpened(startAt);

        ReservationTime reservationTime = ReservationTime.open(request.startAt());
        ReservationTime created = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponse.from(created);
    }

    private void isAlreadyOpened(LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new InvalidArgumentException("이미 존재하는 예약 시간입니다.");
        }
    }

    public List<ReservationTimeResponse> getAll() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return ReservationTimeResponse.from(reservationTimes);
    }

    public void deleteById(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new InvalidArgumentException("해당 시간에 이미 예약이 존재하여 삭제할 수 없습니다.");
        }

        ReservationTime reservationTime = getReservationTime(id);
        reservationTimeRepository.deleteById(reservationTime.getId());
    }

    public ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("예약 시간을 찾을 수 없습니다."));
    }

    public List<AvailableReservationTimeResponse> getAvailableReservationTimes(
            AvailableReservationTimeRequest request) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();

        List<Long> reservedTimeIds = reservationRepository.findReservedTimeIdsByDateAndTheme(
                request.date(), request.themeId());

        return allTimes.stream()
                .map(time -> new AvailableReservationTimeResponse(
                        time.getId(),
                        time.getStartAt(),
                        reservedTimeIds.contains(time.getId())
                ))
                .toList();
    }
}
