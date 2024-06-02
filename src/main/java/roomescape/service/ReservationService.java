package roomescape.service;

import static roomescape.exception.RoomescapeExceptionCode.INVALID_DATETIME;
import static roomescape.exception.RoomescapeExceptionCode.MEMBER_NOT_FOUND;
import static roomescape.exception.RoomescapeExceptionCode.RESERVATION_ALREADY_EXISTS;
import static roomescape.exception.RoomescapeExceptionCode.RESERVATION_NOT_FOUND;
import static roomescape.exception.RoomescapeExceptionCode.THEME_NOT_FOUND;
import static roomescape.exception.RoomescapeExceptionCode.TIME_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.MyReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ReservationWaitingRepository reservationWaitingRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> findAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse reserve(ReservationRequest request) {
        Member member = getMemberById(request.memberId());
        ReservationTime reservationTime = getReservationTimeById(request.timeId());
        Theme theme = getThemeById(request.themeId());
        validateRequest(request, reservationTime);
        Reservation reservation = new Reservation(member, request.date(), reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> searchReservations(
            Long themeId,
            Long memberId,
            LocalDate from,
            LocalDate to
    ) {
        List<Reservation> reservations = reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId,
                from, to);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void cancelReservation(long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        approveReservationWaiting(reservation);
    }

    private void approveReservationWaiting(Reservation reservation) {
        reservationWaitingRepository.findTopByReservationOrderById(reservation)
                .ifPresentOrElse(
                        waiting -> {
                            reservation.updateMember(waiting.getMember());
                            reservationWaitingRepository.delete(waiting);
                        },
                        () -> deleteReservation(reservation)
                );
    }

    public List<MyReservationResponse> findReservationsByMemberId(long memberId) {
        Member member = getMemberById(memberId);
        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    private ReservationTime getReservationTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(TIME_NOT_FOUND));
    }

    private Theme getThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(THEME_NOT_FOUND));
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(MEMBER_NOT_FOUND));
    }

    private Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(RESERVATION_NOT_FOUND));
    }

    private void validateRequest(ReservationRequest request, ReservationTime reservationTime) {
        validateNotPast(request.date(), reservationTime.getStartAt());
        validateNotDuplicatedReservation(request.date(), request.timeId(), request.themeId());
    }

    private void validateNotDuplicatedReservation(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByDateAndReservationTimeIdAndThemeId(date, timeId, themeId)) {
            throw new RoomescapeException(RESERVATION_ALREADY_EXISTS);
        }
    }

    private void validateNotPast(LocalDate date, LocalTime time) {
        LocalDateTime reservationDateTime = date.atTime(time);
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(INVALID_DATETIME);
        }
    }

    private void deleteReservation(Reservation reservation) {
        reservationRepository.delete(reservation);
    }
}
