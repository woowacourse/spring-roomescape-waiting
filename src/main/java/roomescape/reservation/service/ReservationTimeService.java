package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.dto.ReservationTimeDto;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.dto.SaveReservationTimeRequest;
import roomescape.reservation.model.ReservationTimeAvailabilities;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.util.List;

@Transactional
@Service
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeDto> getReservationTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeDto::from)
                .toList();
    }

    public ReservationTimeDto saveReservationTime(final SaveReservationTimeRequest request) {
        validateReservationTimeDuplication(request);

        final ReservationTime savedReservationTime = reservationTimeRepository.save(request.toReservationTime());
        return ReservationTimeDto.from(savedReservationTime);
    }

    private void validateReservationTimeDuplication(final SaveReservationTimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new IllegalArgumentException("이미 존재하는 예약시간이 있습니다.");
        }
    }

    public void deleteReservationTime(final Long reservationTimeId) {
        validateReservationTimeExist(reservationTimeId);
        reservationTimeRepository.deleteById(reservationTimeId);
    }

    private void validateReservationTimeExist(final Long reservationTimeId) {
        if (reservationRepository.existsByTimeId(reservationTimeId)) {
            throw new IllegalArgumentException("예약에 포함된 시간 정보는 삭제할 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public ReservationTimeAvailabilities getAvailableReservationTimes(final LocalDate date, final Long themeId) {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final List<Reservation> reservations = reservationRepository.findAllByDateAndTheme_Id(new ReservationDate(date), themeId);

        return ReservationTimeAvailabilities.of(reservationTimes, reservations);
    }
}
