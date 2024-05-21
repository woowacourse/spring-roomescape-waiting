package roomescape.service;

import org.springframework.stereotype.Component;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.exception.NotExistException;
import roomescape.exception.PastTimeReservationException;
import roomescape.repository.ReservationInfoRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.input.ReservationInfoInput;
import roomescape.util.DateTimeFormatter;

import java.time.LocalDate;

import static roomescape.exception.ExceptionDomainType.RESERVATION_TIME;
import static roomescape.exception.ExceptionDomainType.THEME;

@Component
public class ReservationInfoCreateValidator {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationInfoRepository reservationInfoRepository;
    private final ThemeRepository themeRepository;
    private final DateTimeFormatter nowDateTimeFormatter;

    public ReservationInfoCreateValidator(final ReservationTimeRepository reservationTimeRepository, final ReservationInfoRepository reservationInfoRepository, final ThemeRepository themeRepository, final DateTimeFormatter nowDateTimeFormatter) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationInfoRepository = reservationInfoRepository;
        this.themeRepository = themeRepository;
        this.nowDateTimeFormatter = nowDateTimeFormatter;
    }

    public ReservationInfo validateReservationInput(final ReservationInfoInput input) {
        return reservationInfoRepository.findByDateValueAndTimeIdAndThemeId(
                        LocalDate.parse(input.date()),
                        input.timeId(),
                        input.themeId())
                .orElseGet(() -> createReservationInfo(input));
    }
    
    private ReservationInfo createReservationInfo(final ReservationInfoInput input) {
        final ReservationTime reservationTime = validateExistReservationTime(input.timeId());
        final Theme theme = validateExistTheme(input.themeId());
        final ReservationInfo reservationInfo = ReservationInfo.from(input.date(), reservationTime, theme);

        if (reservationInfo.isBefore(nowDateTimeFormatter.getDate(), nowDateTimeFormatter.getTime())) {
            throw new PastTimeReservationException(reservationInfo.getLocalDateTimeFormat());
        }
        return reservationInfoRepository.save(reservationInfo);
    }

    private ReservationTime validateExistReservationTime(final long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotExistException(RESERVATION_TIME, timeId));
    }

    private Theme validateExistTheme(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotExistException(THEME, themeId));
    }
}
