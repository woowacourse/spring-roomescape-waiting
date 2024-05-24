package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.custom.BadRequestException;
import roomescape.exception.custom.ForbiddenException;
import roomescape.reservation.controller.dto.AvailableTimeResponse;
import roomescape.reservation.controller.dto.ReservationTimeRequest;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.domain.AvailableTimes;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationSlotRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;

@Service
@Transactional
public class ReservationTimeService {

    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationSlotRepository reservationSlotRepository,
                                  ReservationTimeRepository reservationTimeRepository) {
        this.reservationSlotRepository = reservationSlotRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTimeResponse create(ReservationTimeRequest reservationTimeRequest) {
        LocalTime time = LocalTime.parse(reservationTimeRequest.startAt());
        if (reservationTimeRepository.existsByStartAt(time)) {
            throw new ForbiddenException("중복된 예약 시간입니다.");
        }

        ReservationTime reservationTime = new ReservationTime(time);
        return ReservationTimeResponse.from(reservationTimeRepository.save(reservationTime));
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public void delete(long timeId) {
        List<ReservationTime> list = reservationSlotRepository.findAll()
                .stream()
                .map(ReservationSlot::getTime)
                .toList();
        if (reservationSlotRepository.existsByTimeId(timeId)) {
            throw new BadRequestException("예약이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(timeId);
    }

    @Transactional(readOnly = true)
    public List<AvailableTimeResponse> findAvailableTimes(LocalDate date, long themeId) {
        List<ReservationTime> times = reservationTimeRepository.findAll();
        Set<ReservationTime> reservedTimes = reservationTimeRepository.findReservedTime(date, themeId);

        return AvailableTimes.of(times, reservedTimes).getAvailableTimes()
                .stream()
                .map(AvailableTimeResponse::from)
                .toList();
    }
}
