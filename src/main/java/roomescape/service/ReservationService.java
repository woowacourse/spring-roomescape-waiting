package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSearchCondition;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.MyReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse save(ReservationRequest reservationRequest) {
        ReservationTime time = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(ExceptionType.NOT_FOUND_RESERVATION_TIME));
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(ExceptionType.NOT_FOUND_THEME));
        Member member = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new RoomescapeException(ExceptionType.NOT_FOUND_MEMBER));
        LocalDate date = reservationRequest.date();

        validatePastTimeReservation(date, time);
        validateDuplicateReservation(date, time, theme, member);

        ReservationStatus status = determineStatus(time, theme, date);
        Reservation reservation = reservationRepository.save(new Reservation(member, date, time, theme, status));

        return ReservationResponse.from(reservation);
    }

    private void validateDuplicateReservation(LocalDate date, ReservationTime time, Theme theme, Member member) {
        if (reservationRepository.existsByThemeAndDateAndTimeAndReservationMember(theme, date, time, member)) {
            throw new RoomescapeException(ExceptionType.DUPLICATE_RESERVATION);
        }
    }

    private void validatePastTimeReservation(LocalDate date, ReservationTime time) {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(ExceptionType.PAST_TIME_RESERVATION);
        }
    }

    private ReservationStatus determineStatus(ReservationTime requestedTime, Theme requestedTheme, LocalDate date) {
        boolean isAlreadyBooked = reservationRepository.existsByThemeAndDateAndTime(requestedTheme, date, requestedTime);
        if (isAlreadyBooked) {
            return ReservationStatus.PENDING;
        }
        return ReservationStatus.APPROVED;
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findByMemberAndThemeBetweenDates(ReservationSearchCondition condition) {
        List<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                condition.memberId(), condition.themeId(), condition.start(), condition.end());

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findByMemberId(long memberId) {
        return reservationRepository.findAllWithRankByMemberId(memberId)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findByStatus(ReservationStatus status) {
        return reservationRepository.findAllByStatus(status)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void delete(long reservationId, long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomescapeException(ExceptionType.NOT_FOUND_MEMBER));
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomescapeException(ExceptionType.NOT_FOUND_RESERVATION));

        if (member.isAdmin() || reservation.isAuthor(member)) {
            reservationRepository.deleteById(reservationId);
            approveNextReservationAutomatically(reservation);
            return;
        }
        throw new RoomescapeException(ExceptionType.NO_AUTHORITY);
    }

    private void approveNextReservationAutomatically(Reservation reservation) {
        reservationRepository.findFirstByDateAndAndTimeAndTheme(
                reservation.getDate(), reservation.getReservationTime(), reservation.getTheme())
                .ifPresent(firstReservation -> firstReservation.approve());
    }
}
