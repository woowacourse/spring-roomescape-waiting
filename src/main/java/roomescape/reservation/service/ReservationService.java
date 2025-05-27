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
import roomescape.member.repository.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository,
                              WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public ReservationResponse createUserReservation(UserReservationCreateRequest request, Member member) {
        Reservation reservation = buildReservation(request.date(), request.themeId(), request.timeId(), member);
        validateReservation(reservation);
        return saveAndConvertToResponse(reservation);
    }

    @Transactional
    public ReservationResponse createAdminReservation(AdminReservationCreateRequest request) {
        Reservation reservation = buildReservation(request.date(), request.themeId(), request.timeId(),
                request.memberId());
        validateReservation(reservation);
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
        return new Reservation(member, date, time, theme);
    }

    private Reservation buildReservation(LocalDate date, Long themeId, Long timeId, Long memberId) {
        Theme theme = findThemeById(themeId);
        ReservationTime time = findTimeById(timeId);
        Member member = findByMemberId(memberId);
        return new Reservation(member, date, time, theme);
    }

    private Member findByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.MEMBER_NOTFOUND));
    }

    private ReservationTime findTimeById(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.RESERVATION_TIME_NOTFOUND));
    }

    private Theme findThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.THEME_NOTFOUND));
    }

    private void validateAdminReservation(Reservation reservation) {
        validateSlotAvailable(reservation);
        validateFutureDateTime(reservation.getDate(), reservation.getTime().getStartAt());
    }

    private void validateFutureDateTime(LocalDate date, LocalTime time) {
        LocalDateTime requestDateTime = LocalDateTime.of(date, time);
        LocalDateTime now = LocalDateTime.now();
        if (!requestDateTime.isAfter(now)) {
            throw new BadRequestException(ExceptionCause.RESERVATION_INVALID_FOR_PAST);
        }
    }

    private void validateReservation(Reservation reservation) {
        validateNoDuplicateReservation(reservation);
        validateNoDuplicateWaiting(reservation);
        validateSlotAvailable(reservation);
        validateFutureDateTime(reservation.getDate(), reservation.getTime().getStartAt());
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
            throw new ConflictException(ExceptionCause.WAITING_TIME_AND_DATE_DUPLICATE);
        }
    }

    private void validateNoDuplicateReservation(Reservation reservation) {
        if (reservationRepository.existsByMemberAndDateAndTime(reservation.getMember(), reservation.getDate(),
                reservation.getTime())) {
            throw new ConflictException(ExceptionCause.RESERVATION_TIME_AND_DATE_DUPLICATE);
        }
    }

    private ReservationResponse saveAndConvertToResponse(Reservation reservation) {
        Reservation newReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(newReservation, newReservation.getTime(), newReservation.getTheme());
    }
}
