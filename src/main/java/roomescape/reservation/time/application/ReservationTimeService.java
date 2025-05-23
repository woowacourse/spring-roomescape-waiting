package roomescape.reservation.time.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.ReservationRepository;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.reservation.time.presentation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.time.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.time.presentation.dto.ReservationTimeResponse;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationTimeResponse createReservationTime(final ReservationTimeRequest reservationTimeRequest) {
        validateIsDuplicatedTime(reservationTimeRequest);

        final ReservationTime reservationTime = new ReservationTime(reservationTimeRequest.getStartAt());
        return new ReservationTimeResponse(reservationTimeRepository.save(reservationTime));
    }

    public List<ReservationTimeResponse> getReservationTimes() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    @Transactional
    public void deleteReservationTime(final Long id) {
        validateIsDuplicatedReservation(id);

        reservationTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("이미 삭제되어 있는 리소스입니다."));

        reservationTimeRepository.deleteById(id);
    }

    public List<AvailableReservationTimeResponse> getReservationTimes(final LocalDate date, final Long themeId) {
        List<Long> bookedTimeIds = reservationRepository.findAllByDateAndThemeId(date, themeId).stream()
                .map(reservation -> reservation.getReservationTime().getId())
                .toList();

        return reservationTimeRepository.findAll().stream()
                .map(reservationTime -> {
                    boolean alreadyBooked = bookedTimeIds.contains(reservationTime.getId());
                    return new AvailableReservationTimeResponse(reservationTime, alreadyBooked);
                })
                .toList();
    }

    private void validateIsDuplicatedTime(ReservationTimeRequest reservationTimeRequest) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeRequest.getStartAt())) {
            throw new IllegalStateException("중복된 시간은 추가할 수 없습니다.");
        }
    }

    private void validateIsDuplicatedReservation(Long id) {
        if (reservationRepository.existsByReservationTimeId(id)) {
            throw new IllegalStateException("예약이 이미 존재하는 시간입니다.");
        }
    }
}
