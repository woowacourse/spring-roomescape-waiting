package roomescape.reservation.application.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.repository.ReservationRepository;
import roomescape.reservation.application.repository.ReservationTimeRepository;
import roomescape.reservation.presentation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.presentation.dto.ReservationTimeResponse;

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

        return new ReservationTimeResponse(reservationTimeRepository.insert(reservationTimeRequest.getStartAt()));
    }

    public List<ReservationTimeResponse> getReservationTimes() {
        return reservationTimeRepository.findAllTimes().stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    @Transactional
    public void deleteReservationTime(final Long id) {
        validateIsDuplicatedReservation(id);

        if (reservationTimeRepository.delete(id) == 0) {
            throw new IllegalStateException("이미 삭제되어 있는 리소스입니다.");
        }
    }

    public List<AvailableReservationTimeResponse> getReservationTimes(final LocalDate date, final Long themeId) {
        Set<Long> bookedTimeIds = new HashSet<>(reservationRepository.findBookedTimeIds(date, themeId));

        return reservationTimeRepository.findAllTimes().stream()
                .map(reservationTime -> {
                    boolean alreadyBooked = bookedTimeIds.contains(reservationTime.getId());
                    return new AvailableReservationTimeResponse(reservationTime, alreadyBooked);
                })
                .toList();
    }

    private void validateIsDuplicatedTime(ReservationTimeRequest reservationTimeRequest) {
        if (reservationTimeRepository.isExists(reservationTimeRequest.getStartAt())) {
            throw new IllegalStateException("중복된 시간은 추가할 수 없습니다.");
        }
    }

    private void validateIsDuplicatedReservation(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new IllegalStateException("예약이 이미 존재하는 시간입니다.");
        }
    }
}
