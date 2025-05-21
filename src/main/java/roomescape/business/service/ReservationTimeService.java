package roomescape.business.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.ReservationTime;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.infrastructure.repository.ReservationTimeRepository;
import roomescape.presentation.dto.ReservationAvailableTimeResponse;
import roomescape.presentation.dto.ReservationTimeRequest;
import roomescape.presentation.dto.ReservationTimeResponse;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(final ReservationTimeRepository reservationTimeRepository, final ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
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

    @Transactional
    public void deleteById(final Long id) {
        if (!reservationTimeRepository.existsById(id)) {
            throw new NotFoundException("해당하는 방탈출 시간을 찾을 수 없습니다. 방탈출 id: %d".formatted(id));
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationAvailableTimeResponse> findAvailableTimes(final LocalDate date, final Long themeId) {
        final List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, themeId);
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(reservationTime -> {
                    boolean isAlreadyBooked = containPlayTime(reservations, reservationTime);
                    return ReservationAvailableTimeResponse.from(reservationTime, isAlreadyBooked);
                })
                .collect(Collectors.toList());
    }

    private boolean containPlayTime(final List<Reservation> reservations, final ReservationTime reservationTime) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.isSameReservationTime(reservationTime));
    }
}
