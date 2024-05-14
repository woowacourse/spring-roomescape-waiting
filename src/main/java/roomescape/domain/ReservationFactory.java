package roomescape.domain;

import java.time.Clock;
import java.time.LocalDateTime;
import roomescape.application.dto.ReservationRequest;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@DomainService
public class ReservationFactory {
    private final ReservationQueryRepository reservationQueryRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final Clock clock;

    public ReservationFactory(ReservationQueryRepository reservationQueryRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository, MemberQueryRepository memberQueryRepository,
                              Clock clock) {
        this.reservationQueryRepository = reservationQueryRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberQueryRepository = memberQueryRepository;
        this.clock = clock;
    }

    public Reservation create(long memberId, ReservationRequest request) {
        Member member = memberQueryRepository.findById(memberId)
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
        if (reservationQueryRepository.existBy(request.date(), request.timeId(), request.themeId())) {
            throw new RoomescapeException(RoomescapeErrorCode.DUPLICATED_RESERVATION, "이미 존재하는 예약입니다.");
        }
    }
}
