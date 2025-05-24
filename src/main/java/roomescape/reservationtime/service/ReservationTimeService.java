package roomescape.reservationtime.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ReservationException;
import roomescape.reservation.repository.RoomEscapeInformationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final RoomEscapeInformationRepository roomEscapeInformationRepository;

    public ReservationTimeResponse saveTime(final ReservationTimeRequest request) {
        final ReservationTime reservationTime = reservationTimeRepository.save(ReservationTime.from(request.startAt()));
        return new ReservationTimeResponse(reservationTime);
    }

    public List<ReservationTimeResponse> findAll() {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    @Transactional
    public void delete(final Long id) {
        if (roomEscapeInformationRepository.existsByTimeId(id)) {
            throw new ReservationException("해당 시간으로 예약된 건이 존재합니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
