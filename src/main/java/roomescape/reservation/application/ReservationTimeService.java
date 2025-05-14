package roomescape.reservation.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.impl.BadRequestException;
import roomescape.reservation.application.dto.ReservationTimeRequest;
import roomescape.reservation.application.dto.ReservationTimeResponse;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(final ReservationRepository reservationRepository,
                                  final ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTimeResponse> findAll() {
        final List<ReservationTime> times = reservationTimeRepository.findAll();
        return times.stream()
                .map(ReservationTimeResponse::of)
                .toList();
    }

    public ReservationTimeResponse add(final ReservationTimeRequest requestDto) {
        if (reservationTimeRepository.existsByStartAt(requestDto.startAt())) {
            throw new BadRequestException("동일한 시간이 이미 존재합니다.");
        }
        final ReservationTime reservationTime = new ReservationTime(requestDto.startAt());
        final ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.of(savedReservationTime);
    }

    public void deleteById(final Long id) {
        if (reservationRepository.existsByTime_Id(id)) {
            throw new BadRequestException("이 시간의 예약이 존재합니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
