package roomescape.service;

import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.NotExistException;
import roomescape.exception.PastTimeReservationException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.input.ReservationInput;
import roomescape.util.DateTimeFormatter;

import static roomescape.exception.ExceptionDomainType.*;

@Component
public class ReservationCreateValidator {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final DateTimeFormatter nowDateTimeFormatter;


    public ReservationCreateValidator(final ReservationRepository reservationRepository, final ReservationTimeRepository reservationTimeRepository, final ThemeRepository themeRepository, final MemberRepository memberDao, final DateTimeFormatter nowDateTimeFormatter) {
        this.reservationRepository = reservationRepository;
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
