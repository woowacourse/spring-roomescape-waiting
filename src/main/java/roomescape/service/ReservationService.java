package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSearchCondition;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.LoginMemberReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;

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
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION_TIME));
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));
        Member member = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));

        validatePastTimeReservation(reservationRequest.date(), time);
        ReservationStatus status = determineStatus(time, theme, reservationRequest.date());
        Reservation beforeSave = new Reservation(member, reservationRequest.date(), time, theme, status);
        Reservation saved = reservationRepository.save(beforeSave);

        return ReservationResponse.from(saved);
    }

    private ReservationStatus determineStatus(ReservationTime requestedTime, Theme requestedTheme, LocalDate date) {
        boolean isAlreadyBooked = reservationRepository.existsByThemeAndDateAndTime(requestedTheme, date, requestedTime);
        if (isAlreadyBooked) {
            return ReservationStatus.PENDING;
        }
        return ReservationStatus.APPROVED;
    }

    private void validatePastTimeReservation(LocalDate date, ReservationTime time) {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(PAST_TIME_RESERVATION);
        }
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

        return reservations
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<LoginMemberReservationResponse> findByMemberId(long memberId) {
        return reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(LoginMemberReservationResponse::from)
                .toList();
    }

    public void delete(long id) {
        reservationRepository.deleteById(id);
    }
}
