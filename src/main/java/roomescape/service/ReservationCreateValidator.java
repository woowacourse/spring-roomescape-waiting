package roomescape.service;

import org.springframework.stereotype.Component;
import roomescape.dao.MemberDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.NotExistException;
import roomescape.exception.PastTimeReservationException;
import roomescape.service.dto.input.ReservationInput;
import roomescape.util.DateTimeFormatter;

import static roomescape.exception.ExceptionDomainType.*;

@Component
public class ReservationCreateValidator {
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final MemberDao memberDao;
    private final DateTimeFormatter nowDateTimeFormatter;


    public ReservationCreateValidator(final ReservationDao reservationDao, final ReservationTimeDao reservationTimeDao, final ThemeDao themeDao, final MemberDao memberDao, final DateTimeFormatter nowDateTimeFormatter) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.memberDao = memberDao;
        this.nowDateTimeFormatter = nowDateTimeFormatter;
    }

    public Reservation validateReservationInput(final ReservationInput input) {
        final ReservationTime reservationTime = validateExistReservationTime(input.timeId());
        final Theme theme = validateExistTheme(input.themeId());
        final Member member = validateExistMember(input.memberId());

        final Reservation reservation = input.toReservation(reservationTime, theme,member);
        if (reservationDao.isExistByReservationAndTime(ReservationDate.from(input.date()), input.timeId())) {
            throw new AlreadyExistsException(RESERVATION, reservation.getLocalDateTimeFormat());
        }
        if (reservation.isBefore(nowDateTimeFormatter.getDate(), nowDateTimeFormatter.getTime())) {
            throw new PastTimeReservationException(reservation.getLocalDateTimeFormat());
        }
        return reservation;
    }

    private ReservationTime validateExistReservationTime(final long timeId) {
        return reservationTimeDao.find(timeId)
                .orElseThrow(() -> new NotExistException(RESERVATION_TIME, timeId));
    }

    private Theme validateExistTheme(final long themeId) {
        return themeDao.find(themeId)
                .orElseThrow(() -> new NotExistException(THEME, themeId));
    }

    private Member validateExistMember(final long memberId) {
        return memberDao.findById(memberId)
                .orElseThrow(() -> new NotExistException(MEMBER, memberId));
    }
}
