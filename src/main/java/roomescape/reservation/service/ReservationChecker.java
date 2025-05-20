package roomescape.reservation.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.exception.BadRequestException;
import roomescape.exception.ExceptionCause;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.UserReservationRequest;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Component
public class ReservationChecker {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationChecker(ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Reservation createReservationWithoutId(UserReservationRequest dto, Member member) {
        ReservationRequest request = new ReservationRequest(dto.date(), dto.timeId(), dto.themeId(), member.getId());
        return createReservationWithoutId(request);
    }

    public Reservation createReservationWithoutId(ReservationRequest dto) {
        ReservationTime reservationTime = reservationTimeRepository.findById(dto.timeId())
                .orElseThrow(() -> new NotFoundException(ExceptionCause.RESERVATION_TIME_NOTFOUND));

        validateRequestDateTime(LocalDateTime.of(dto.date(), reservationTime.getStartAt()));

        Theme theme = themeRepository.findById(dto.themeId())
                .orElseThrow(() -> new NotFoundException(ExceptionCause.THEME_NOTFOUND));

        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new NotFoundException(ExceptionCause.MEMBER_NOTFOUND));

        return dto.createWithoutId(reservationTime, theme, member, ReservationStatus.RESERVED);
    }

    private void validateRequestDateTime(LocalDateTime requestDateTime) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (requestDateTime.isBefore(currentDateTime) || requestDateTime.equals(currentDateTime)) {
            throw new BadRequestException(ExceptionCause.RESERVATION_INVALID_FOR_PAST);
        }
    }
}
