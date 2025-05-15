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
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> getAll() {
        List<Reservation> reservations = reservationRepository.findAll();

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
        validateDateTime(now, reservation.getDate(), reservation.getTime().getStartAt());

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> findReservationByFiltering(final FilteringReservationRequest request) {
        Long themeId = request.themeId();
        Long memberId = request.memberId();
        LocalDate dateFrom = request.dateFrom();
        LocalDate dateTo = request.dateTo();

        return reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    private boolean isAlreadyBooked(final ReservationRequest request) {
        return reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId()
        );
    }

    private Reservation getReservation(final ReservationRequest request, final Member member) {
        ReservationTime reservationTime = gerReservationTime(request);
        Theme theme = getTheme(request);

        return new Reservation(member, request.date(), reservationTime, theme);
    }

    private Theme getTheme(final ReservationRequest request) {
        Long themeId = request.themeId();
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("theme not found id =" + themeId));
    }

    private ReservationTime gerReservationTime(final ReservationRequest request) {
        Long timeId = request.timeId();
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new EntityNotFoundException("reservationsTime not found id =" + timeId));
    }

    private void validateDateTime(final LocalDateTime now, final LocalDate date, final LocalTime time) {
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        if (now.isAfter(dateTime)) {
            throw new IllegalArgumentException("이미 지난 예약 시간입니다.");
        }
    }

    public void delete(final Long id) {
        // TODO: 존재 여부 확인해서 EntityNotFoundException 검증
        reservationRepository.deleteById(id);
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

        return reservationTimeRepository.findAll()
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
        return reservationRepository.findByDateAndThemeId(date, themeId)
                .stream()
                .map(Reservation::getTime)
                .collect(Collectors.toSet());
    }
}
