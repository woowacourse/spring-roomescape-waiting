package roomescape.domain.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import roomescape.dto.ReservationRequest;
import roomescape.domain.DomainService;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@DomainService
public class ReservationFactory {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public ReservationFactory(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository, MemberRepository memberRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public Reservation create(long memberId, ReservationRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_MEMBER));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_THEME));
        ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_TIME));
        LocalDateTime dateTime = LocalDateTime.of(request.date(), reservationTime.getStartAt());
        validateRequestDateAfterCurrentTime(dateTime);
        validateUniqueReservation(request);
        return request.toReservation(member, reservationTime, theme);
    }

    private void validateRequestDateAfterCurrentTime(LocalDateTime dateTime) {
        LocalDateTime currentTime = LocalDateTime.now(clock);
        if (dateTime.isBefore(currentTime)) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "현재 시간보다 과거로 예약할 수 없습니다.");
        }
    }

    private void validateUniqueReservation(ReservationRequest request) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId())
        ) {
            throw new RoomescapeException(RoomescapeErrorCode.DUPLICATED_RESERVATION, "이미 존재하는 예약입니다.");
        }
    }
}
