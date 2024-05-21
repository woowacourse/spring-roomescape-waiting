package roomescape.service.finder;

import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;

import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequest;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationFinder {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;

    public ReservationFinder(ReservationRepository reservationRepository,
                             ReservationTimeRepository reservationTimeRepository, MemberRepository memberRepository,
                             ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
    }

    public Reservation findByReservationRequest(ReservationRequest reservationRequest,
                                                Supplier<? extends RuntimeException> exceptionSupplier) {
        ReservationTime requestedTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION_TIME));
        Theme requestedTheme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));
        memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));
        return reservationRepository.findByThemeAndDateAndTime(requestedTheme, reservationRequest.date(), requestedTime)
                .orElseThrow(exceptionSupplier);
    }

    public Reservation createWhenNotExists(ReservationRequest reservationRequest) {
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION_TIME));
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));
        Member member = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));
        if (reservationRepository.existsByThemeAndDateAndTime(theme, reservationRequest.date(), reservationTime)) {
            throw new RoomescapeException(DUPLICATE_RESERVATION);
        }
        return new Reservation(member, reservationRequest.date(), reservationTime, theme);
    }
}
