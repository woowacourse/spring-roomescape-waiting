package roomescape.service.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSpecification;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.InvalidDateTimeReservationException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.service.reservation.dto.ReservationListResponse;
import roomescape.service.reservation.dto.ReservationMineListResponse;
import roomescape.service.reservation.dto.ReservationMineResponse;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;

@Service
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ReservationListResponse findAllReservation(
            Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        Specification<Reservation> specification = ReservationSpecification.applyFiltersForSearch(
                memberId, themeId, dateFrom, dateTo);
        List<Reservation> reservations = reservationRepository.findAll(specification);
        return new ReservationListResponse(reservations.stream()
                .map(ReservationResponse::new)
                .toList());
    }

    @Transactional(readOnly = true)
    public ReservationMineListResponse findMyReservation(Member member) {
        List<Reservation> reservations = reservationRepository.findByMemberId(member.getId());
        return new ReservationMineListResponse(reservations.stream()
                .map(ReservationMineResponse::new)
                .toList());
    }

    public ReservationResponse saveReservation(ReservationRequest request, Member member) {
        validateDuplicateReservation(request);
        ReservationTime time = findReservationTimeById(request.getTimeId());
        Theme theme = findThemeById(request.getThemeId());
        validateDateTimeReservation(request, time);

        Reservation reservation = request.toReservation(ReservationStatus.BOOKED, time, theme, member);
        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    private void validateDuplicateReservation(ReservationRequest request) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.getDate(), request.getTimeId(), request.getThemeId())) {
            throw new DuplicatedReservationException();
        }
    }

    private void validateDateTimeReservation(ReservationRequest request, ReservationTime time) {
        LocalDateTime localDateTime = request.getDate().atTime(time.getStartAt());
        if (localDateTime.isBefore(LocalDateTime.now(clock))) {
            throw new InvalidDateTimeReservationException();
        }
    }

    public void deleteReservation(long id) {
        Reservation reservation = findReservationById(id);
        reservationRepository.delete(reservation);
    }

    private Reservation findReservationById(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }

    private ReservationTime findReservationTimeById(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(NotFoundTimeException::new);
    }

    private Theme findThemeById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(NotFoundThemeException::new);
    }
}
