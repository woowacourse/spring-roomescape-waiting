package roomescape.time.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.IllegalRequestException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.dto.AvailableTimeResponse;
import roomescape.time.dto.ReservationTimeAddRequest;
import roomescape.time.dto.ReservationTimeResponse;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    @Transactional(readOnly = false)
    public ReservationTime findById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalRequestException("해당하는 예약시간이 존재하지 않습니다 ID: " + id));
    }

    @Transactional(readOnly = false)
    public List<ReservationTimeResponse> findAllReservationTime() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    @Transactional(readOnly = false)
    public List<AvailableTimeResponse> findAllWithReservationStatus(LocalDate date, Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<ReservationTime> reservedTime = reservationTimeRepository.findReservedTime(date, themeId);

        return reservationTimes.stream()
                .map(time -> new AvailableTimeResponse(time, reservedTime.contains(time)))
                .toList();
    }

    @Transactional
    public ReservationTimeResponse saveReservationTime(ReservationTimeAddRequest reservationTimeAddRequest) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeAddRequest.startAt())) {
            throw new IllegalRequestException("이미 존재하는 예약시간은 추가할 수 없습니다.");
        }
        ReservationTime saved = reservationTimeRepository.save(reservationTimeAddRequest.toReservationTime());
        return new ReservationTimeResponse(saved);
    }

    @Transactional
    public void removeReservationTime(Long id) {
        reservationTimeRepository.deleteById(id);
    }
}
