package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.BookedReservationTimeResponse;
import roomescape.reservation.dto.FilteringReservationRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ReservationTimeDao;
import roomescape.reservation.repository.ThemeDao;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;

    public ReservationService(
            final ReservationDao reservationDao,
            final ReservationTimeDao reservationTimeDao,
            final ThemeDao themeDao
    ) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
    }

    public List<ReservationResponse> getAll() {
        List<Reservation> reservations = reservationDao.findAll();

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse create(final ReservationRequest request, final Member member) {
        if (isAlreadyBooked(request)) {
            throw new AlreadyInUseException("reservation is already in use");
        }

        Reservation reservation = getReservation(request, member);
        LocalDateTime now = LocalDateTime.now();
        validateDateTime(now, reservation.getReservationDate(), reservation.getReservationStartTime());

        Reservation savedReservation = reservationDao.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> findReservationByFiltering(final FilteringReservationRequest request) {
        Long themeId = request.themeId();
        Long memberId = request.memberId();
        LocalDate dateFrom = request.dateFrom();
        LocalDate dateTo = request.dateTo();

        return reservationDao.findByThemeIdAndMemberIdAndBetweenDate(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    private boolean isAlreadyBooked(final ReservationRequest request) {
        return reservationDao.existsByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId()
        );
    }

    private Reservation getReservation(final ReservationRequest request, final Member member) {
        ReservationTime reservationTime = gerReservationTime(request);
        Theme theme = getTheme(request);

        return Reservation.withoutId(member, request.date(), reservationTime, theme);
    }

    private Theme getTheme(final ReservationRequest request) {
        Long themeId = request.themeId();
        return themeDao.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("theme not found id =" + themeId));
    }

    private ReservationTime gerReservationTime(final ReservationRequest request) {
        Long timeId = request.timeId();
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new EntityNotFoundException("reservationsTime not found id =" + timeId));
    }

    private void validateDateTime(final LocalDateTime now, final LocalDate date, final LocalTime time) {
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        if (now.isAfter(dateTime)) {
            throw new IllegalArgumentException("이미 지난 예약 시간입니다.");
        }
    }

    public void delete(final Long id) {
        int updatedRow = reservationDao.deleteById(id);

        if (updatedRow == 0) {
            throw new EntityNotFoundException("reservation not found");
        }
    }

    public List<BookedReservationTimeResponse> getAvailableTimes(final LocalDate date, final Long themeId) {
        Map<ReservationTime, Boolean> allTimes = processAlreadyBookedTimesMap(date, themeId);

        return allTimes.entrySet()
                .stream()
                .map(entry -> bookedReservationTimeResponseOf(entry.getKey(), entry.getValue()))
                .toList();
    }

    private Map<ReservationTime, Boolean> processAlreadyBookedTimesMap(final LocalDate date, final Long themeId) {
        Set<ReservationTime> alreadyBookedTimes = getAlreadyBookedTimes(date, themeId);

        return reservationTimeDao.findAll()
                .stream()
                .collect(Collectors.toMap(Function.identity(), alreadyBookedTimes::contains));
    }

    private BookedReservationTimeResponse bookedReservationTimeResponseOf(
            final ReservationTime reservationTime,
            final boolean isAlreadyBooked
    ) {
        return new BookedReservationTimeResponse(ReservationTimeResponse.from(reservationTime), isAlreadyBooked);
    }

    private Set<ReservationTime> getAlreadyBookedTimes(final LocalDate date, final Long themeId) {
        return reservationDao.findByDateAndThemeId(date, themeId)
                .stream()
                .map(Reservation::getReservationTime)
                .collect(Collectors.toSet());
    }
}
