package roomescape.service;

import static roomescape.exception.ExceptionDomainType.MEMBER;
import static roomescape.exception.ExceptionDomainType.RESERVATION;
import static roomescape.exception.ExceptionDomainType.RESERVATION_TIME;
import static roomescape.exception.ExceptionDomainType.THEME;
import static roomescape.exception.ExceptionDomainType.WAITING;

import java.time.LocalTime;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.user.Member;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.InvalidWaitingException;
import roomescape.exception.NotExistException;
import roomescape.exception.PastTimeReservationException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.WaitingInput;
import roomescape.util.DateTimeFormatter;

@Component
public class CreateValidator {
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final DateTimeFormatter nowDateTimeFormatter;


    public CreateValidator(final ReservationRepository reservationRepository,
                           final WaitingRepository waitingRepository,
                           final ReservationTimeRepository reservationTimeRepository,
                           final ThemeRepository themeRepository, final MemberRepository memberDao,
                           final DateTimeFormatter nowDateTimeFormatter) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberDao;
        this.nowDateTimeFormatter = nowDateTimeFormatter;
    }

    public Reservation validateReservationInput(final ReservationInput input) {
        final ReservationTime reservationTime = validateExistReservationTime(input.timeId());
        final Theme theme = validateExistTheme(input.themeId());
        final Member member = validateExistMember(input.memberId());

        final Reservation reservation = input.toReservation(reservationTime, theme, member);
        if (reservationRepository.existsByDateAndTimeId(ReservationDate.from(input.date()), input.timeId())) {
            throw new AlreadyExistsException(RESERVATION, reservation.getLocalDateTimeFormat());
        }
        if (reservation.isBefore(nowDateTimeFormatter.getDate(), nowDateTimeFormatter.getTime())) {
            throw new PastTimeReservationException(reservation.getLocalDateTimeFormat());
        }
        return reservation;
    }

    public Waiting validateWaitingInput(final WaitingInput input) {
        final ReservationTime reservationTime = validateExistReservationTime(input.timeId());
        final Theme theme = validateExistTheme(input.themeId());
        final Member member = validateExistMember(input.memberId());

        final Waiting waiting = input.toWaiting(reservationTime, theme, member, LocalTime.now());
        if (!reservationRepository.existsByDateAndTimeId(ReservationDate.from(input.date()), input.timeId())) {
            throw new InvalidWaitingException();
        }
        if (waitingRepository.existsByDateAndTimeIdAndMemberId(ReservationDate.from(input.date()), input.timeId(),
                input.memberId())) {
            throw new AlreadyExistsException(WAITING, waiting.getLocalDateTimeFormat());
        }
        if (reservationRepository.existsByDateAndTimeIdAndMemberId(ReservationDate.from(input.date()), input.timeId(),
                input.memberId())) {
            throw new AlreadyExistsException(RESERVATION, waiting.getLocalDateTimeFormat());
        }
        if (waiting.isBefore(nowDateTimeFormatter.getDate(), nowDateTimeFormatter.getTime())) {
            throw new PastTimeReservationException(waiting.getLocalDateTimeFormat());
        }
        return waiting;
    }

    private ReservationTime validateExistReservationTime(final long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotExistException(RESERVATION_TIME, timeId));
    }

    private Theme validateExistTheme(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotExistException(THEME, themeId));
    }

    private Member validateExistMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotExistException(MEMBER, memberId));
    }
}
