package roomescape.core.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;
import roomescape.core.dto.reservationtime.BookedTimeResponse;
import roomescape.core.dto.reservationtime.ReservationTimeRequest;
import roomescape.core.dto.reservationtime.ReservationTimeResponse;
import roomescape.core.repository.ReservationRepository;
import roomescape.core.repository.ReservationTimeRepository;
import roomescape.core.repository.ThemeRepository;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeService(final ReservationTimeRepository reservationTimeRepository,
                                  final ReservationRepository reservationRepository,
                                  final ThemeRepository themeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ReservationTimeResponse create(final ReservationTimeRequest request) {
        final ReservationTime reservationTime = new ReservationTime(request.getStartAt());
        validateDuplicatedStartAt(reservationTime);
        final ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return new ReservationTimeResponse(savedReservationTime.getId(), savedReservationTime);
    }

    private void validateDuplicatedStartAt(final ReservationTime reservationTime) {
        final Integer reservationTimeCount = reservationTimeRepository.countByStartAt(
                reservationTime.getStartAt());
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
        final Theme theme = themeRepository.findById(themeId).orElseThrow(IllegalArgumentException::new);
        final List<Reservation> reservations = reservationRepository.findAllByDateAndTheme(LocalDate.parse(date),
                theme);
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
                .anyMatch(reservation -> Objects.equals(reservation.getReservationTime(), reservationTime));
    }

    @Transactional
    public void delete(final long id) {
        final ReservationTime time = reservationTimeRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
        final int reservationCount = reservationRepository.countByTime(time);
        if (reservationCount > 0) {
            throw new IllegalArgumentException("예약 내역이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
