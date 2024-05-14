package roomescape.core.service;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.dto.reservationtime.BookedTimeResponse;
import roomescape.core.dto.reservationtime.ReservationTimeRequest;
import roomescape.core.dto.reservationtime.ReservationTimeResponse;
import roomescape.core.repository.ReservationRepository;
import roomescape.core.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(final ReservationTimeRepository reservationTimeRepository,
                                  final ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationTimeResponse create(final ReservationTimeRequest request) {
        final ReservationTime reservationTime = new ReservationTime(request.getStartAt());
        validateDuplicatedStartAt(reservationTime);
        final Long id = reservationTimeRepository.save(reservationTime);
        return new ReservationTimeResponse(id, reservationTime);
    }

    private void validateDuplicatedStartAt(final ReservationTime reservationTime) {
        final Integer reservationTimeCount = reservationTimeRepository.countByStartAt(
                reservationTime.getStartAtString());
        if (reservationTimeCount > 0) {
            throw new IllegalArgumentException("해당 시간이 이미 존재합니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookedTimeResponse> findAllWithBookable(final String date, final long themeId) {
        final List<Reservation> reservations = reservationRepository.findAllByDateAndThemeId(date, themeId);
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(reservationTime -> findBookedTimes(reservationTime, reservations))
                .toList();
    }

    private BookedTimeResponse findBookedTimes(final ReservationTime reservationTime,
                                               final List<Reservation> reservations) {
        return new BookedTimeResponse(reservationTime, isAlreadyBooked(reservationTime, reservations));
    }

    private boolean isAlreadyBooked(final ReservationTime reservationTime, final List<Reservation> reservations) {
        return reservations.stream()
                .anyMatch(reservation -> Objects.equals(reservation.getTimeId(), reservationTime.getId()));
    }

    @Transactional
    public void delete(final long id) {
        final int reservationCount = reservationRepository.countByTimeId(id);
        if (reservationCount > 0) {
            throw new IllegalArgumentException("예약 내역이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
