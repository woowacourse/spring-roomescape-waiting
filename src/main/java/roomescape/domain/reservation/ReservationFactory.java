package roomescape.domain.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import roomescape.domain.DomainService;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.RoomescapeException;

@DomainService
public class ReservationFactory {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public ReservationFactory(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public Reservation create(Long memberId, LocalDate date, Long timeId, Long themeId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomescapeException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomescapeException(HttpStatus.NOT_FOUND, "존재하지 않는 테마입니다."));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new RoomescapeException(HttpStatus.NOT_FOUND, "존재하지 않는 예약 시간입니다."));
        LocalDateTime dateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        validateRequestDateAfterCurrentTime(dateTime);
        if (checkUniqueReservation(date, timeId, themeId)) {
            validateIsExistMyReservation(date, timeId, themeId, memberId);
            return new Reservation(member, date, reservationTime, theme, Status.WAITING);
        }
        return new Reservation(member, date, reservationTime, theme, Status.RESERVATION);
    }

    private void validateRequestDateAfterCurrentTime(LocalDateTime dateTime) {
        LocalDateTime currentTime = LocalDateTime.now(clock);
        if (dateTime.isBefore(currentTime)) {
            throw new RoomescapeException(HttpStatus.BAD_REQUEST, "현재 시간보다 과거로 예약할 수 없습니다.");
        }
    }

    private boolean checkUniqueReservation(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    private void validateIsExistMyReservation(LocalDate date, Long timeId, Long themeId, Long memberId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId)) {
            throw new RoomescapeException(HttpStatus.CONFLICT, "이미 예약을 했습니다.");
        }
    }
}
