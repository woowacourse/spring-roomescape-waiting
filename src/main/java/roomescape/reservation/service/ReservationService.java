package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.exception.ConflictException;
import roomescape.exception.ExceptionCause;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.AdminReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.UserReservationCreateRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.waiting.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public ReservationResponse createUserReservation(UserReservationCreateRequest request, Member member) {
        Reservation reservation = buildReservation(request.date(), request.themeId(), request.timeId(), member);
        validateUserReservation(reservation);
        return saveAndConvertToResponse(reservation);
    }

    @Transactional
    public ReservationResponse createAdminReservation(AdminReservationCreateRequest request, Member member) {
        Reservation reservation = buildReservation(request.date(), request.themeId(), request.timeId(), member);
        return saveAndConvertToResponse(reservation);
    }

    public List<ReservationResponse> findAllReservations() {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .map(reservation -> ReservationResponse.from(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    public List<ReservationResponse> searchReservations(Long memberId, Long themeId, LocalDate from, LocalDate to) {
        List<Reservation> searchResults = reservationRepository.findByMemberIdAndThemeIdAndDateRange(memberId, themeId,
                from, to);

        return searchResults.stream()
                .map(reservation -> ReservationResponse.from(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    @Transactional
    public void deleteReservation(Long id) {
        if (reservationRepository.findById(id).isEmpty()) {
            throw new NotFoundException(ExceptionCause.RESERVATION_NOTFOUND);
        }

        reservationRepository.deleteById(id);
    }

    private Reservation buildReservation(LocalDate date, Long themeId, Long timeId, Member member) {
        Theme theme = findThemeById(themeId);
        ReservationTime time = findTimeById(timeId);
        validateFutureDateTime(date, time.getStartAt());
        return new Reservation(member, date, time, theme);
    }

    private ReservationTime findTimeById(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.RESERVATION_TIME_NOTFOUND));
    }

    private Theme findThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.THEME_NOTFOUND));
    }

    private void validateFutureDateTime(LocalDate date, LocalTime time) {
        LocalDateTime requestDateTime = LocalDateTime.of(date, time);
        LocalDateTime now = LocalDateTime.now();
        if (!requestDateTime.isAfter(now)) {
            throw new BadRequestException(ExceptionCause.RESERVATION_INVALID_FOR_PAST);
        }
    }

    private void validateUserReservation(Reservation reservation) {
        validateNoDuplicateReservation(reservation);
        validateNoDuplicateWaiting(reservation);
        validateSlotAvailable(reservation);
    }

    private void validateSlotAvailable(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId())) {
            throw new ConflictException(ExceptionCause.RESERVATION_ALREADY_BOOKED);
        }
    }

    private void validateNoDuplicateWaiting(Reservation reservation) {
        if (waitingRepository.existsByMemberAndDateAndTime(reservation.getMember(), reservation.getDate(),
                reservation.getTime())) {
            throw new BadRequestException(ExceptionCause.WAITING_TIME_AND_DATE_DUPLICATE);
        }
    }

    private void validateNoDuplicateReservation(Reservation reservation) {
        if (reservationRepository.existsByMemberAndDateAndTime(reservation.getMember(), reservation.getDate(),
                reservation.getTime())) {
            throw new BadRequestException(ExceptionCause.RESERVATION_TIME_AND_DATE_DUPLICATE);
        }
    }

    private ReservationResponse saveAndConvertToResponse(Reservation reservation) {
        Reservation newReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(newReservation, newReservation.getTime(), newReservation.getTheme());
    }
}
