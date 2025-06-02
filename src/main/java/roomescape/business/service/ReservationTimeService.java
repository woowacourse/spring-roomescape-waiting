package roomescape.business.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.WaitInfo;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.WaitInfoRepository;
import roomescape.presentation.dto.ReservationAvailableTimeResponse;
import roomescape.presentation.dto.ReservationTimeRequest;
import roomescape.presentation.dto.ReservationTimeResponse;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final WaitInfoRepository waitInfoRepository;

    public ReservationTimeService(
            final ReservationTimeRepository reservationTimeRepository,
            final WaitInfoRepository waitInfoRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitInfoRepository = waitInfoRepository;
    }

    public ReservationTimeResponse insert(final ReservationTimeRequest reservationTimeRequest) {
        validateStartAtIsNotDuplicate(reservationTimeRequest.startAt());
        final ReservationTime reservationTime = reservationTimeRequest.toDomain();
        final ReservationTime insertReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(insertReservationTime);
    }

    private void validateStartAtIsNotDuplicate(final LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new DuplicateException("추가 하려는 시간이 이미 존재합니다.");
        }
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse findById(final Long id) {
        final ReservationTime reservationTime = reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당하는 방탈출 시간을 찾을 수 없습니다. 방탈출 id: %d".formatted(id)));
        return ReservationTimeResponse.from(reservationTime);
    }

    public void deleteById(final Long id) {
        if (!reservationTimeRepository.existsById(id)) {
            throw new NotFoundException("해당하는 방탈출 시간을 찾을 수 없습니다. 방탈출 id: %d".formatted(id));
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationAvailableTimeResponse> findAvailableTimes(final LocalDate date, final Long themeId) {
        final List<WaitInfo> waitInfos = waitInfoRepository.findByRank(1L);
        final List<Reservation> alreadyBookedReservations = waitInfos.stream()
                .map(WaitInfo::getReservation)
                .filter(reservation -> reservation.getDate().equals(date)
                        && reservation.getTheme().getId().equals(themeId))
                .toList();

        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(reservationTime -> {
                    boolean isAlreadyBooked =
                            isAlreadyBookedReservationTime(reservationTime, alreadyBookedReservations);
                    return new ReservationAvailableTimeResponse(reservationTime, isAlreadyBooked);
                })
                .toList();
    }

    private boolean isAlreadyBookedReservationTime(
            final ReservationTime reservationTime,
            final List<Reservation> reservations
    ) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.isSameReservationTime(reservationTime));
    }
}
