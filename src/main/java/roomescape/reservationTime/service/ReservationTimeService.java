package roomescape.reservationTime.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.ForeignKeyException;
import roomescape.common.exception.InvalidIdException;
import roomescape.common.exception.message.IdExceptionMessage;
import roomescape.common.exception.message.ReservationTimeExceptionMessage;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.reservationTime.dto.admin.ReservationTimeRequest;
import roomescape.reservationTime.dto.admin.ReservationTimeResponse;
import roomescape.reservationTime.dto.user.AvailableReservationTimeRequest;
import roomescape.reservationTime.dto.user.AvailableReservationTimeResponse;
import roomescape.theme.domain.repository.ThemeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeService(
            ReservationTimeRepository timeRepository,
            ReservationRepository reservationRepository,
            ThemeRepository themeRepository
    ) {
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationTimeResponse> findAll() {
        return timeRepository.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findByDateAndTheme(
            final AvailableReservationTimeRequest availableReservationTimeRequest
    ) {
        searchTheme(availableReservationTimeRequest);
        List<ReservationTime> reservedTimes = findReservedTimes(availableReservationTimeRequest);
        Set<Long> reservedIds = findReservedTimeIds(reservedTimes);

        List<ReservationTime> availableReservationTimes = timeRepository.findAll();
        return availableReservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTimeResponse(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        reservedIds.contains(reservationTime.getId())
                ))
                .toList();
    }

    private void searchTheme(
            final AvailableReservationTimeRequest availableReservationTimeRequest
    ) {
        themeRepository.findById(availableReservationTimeRequest.themeId())
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_THEME_ID.getMessage()));
    }

    private List<ReservationTime> findReservedTimes(
            final AvailableReservationTimeRequest availableReservationTimeRequest
    ) {
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.isSameDate(availableReservationTimeRequest.date())
                        && reservation.isSameTheme(availableReservationTimeRequest.themeId())
                ).map(reservation -> new ReservationTime(
                        reservation.getTime().getId(),
                        reservation.getTime().getStartAt())
                )
                .toList();
    }

    private Set<Long> findReservedTimeIds(final List<ReservationTime> reservedTimes) {
        return reservedTimes.stream()
                .map(ReservationTime::getId)
                .collect(Collectors.toSet());
    }

    public ReservationTimeResponse add(final ReservationTimeRequest reservationTimeRequest) {
        validateDuplicate(reservationTimeRequest);

        ReservationTime newReservationTime = new ReservationTime(reservationTimeRequest.startAt());
        ReservationTime savedReservationTime = timeRepository.save(newReservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    private void validateDuplicate(final ReservationTimeRequest reservationTimeRequest) {
        boolean isDuplicate = timeRepository.existsByStartAt(reservationTimeRequest.startAt());

        if (isDuplicate) {
            throw new DuplicateException(ReservationTimeExceptionMessage.DUPLICATE_TIME.getMessage());
        }
    }

    public void deleteById(final Long id) {
        searchReservationTimeId(id);
        validateUnoccupiedTime(id);
        timeRepository.deleteById(id);
    }

    private void searchReservationTimeId(final Long id) {
        timeRepository.findById(id)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_TIME_ID.getMessage()));
    }

    private void validateUnoccupiedTime(final Long id) {
        boolean isOccupiedTimeId = timeRepository.existsById(id);

        if (isOccupiedTimeId) {
            throw new ForeignKeyException(ReservationTimeExceptionMessage.RESERVED_TIME.getMessage());
        }
    }
}
