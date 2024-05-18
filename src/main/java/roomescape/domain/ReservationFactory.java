package roomescape.domain;

import java.time.Clock;
import java.time.LocalDateTime;
import roomescape.application.dto.ReservationRequest;
import roomescape.domain.repository.MemberQueryRepository;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.ReservationTimeCommandRepository;
import roomescape.domain.repository.ReservationTimeQueryRepository;
import roomescape.domain.repository.ThemeCommandRepository;
import roomescape.domain.repository.ThemeQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@DomainService
public class ReservationFactory {

    private final ReservationQueryRepository reservationQueryRepository;
    private final ReservationTimeCommandRepository reservationTimeCommandRepository;
    private final ReservationTimeQueryRepository reservationTimeQueryRepository;
    private final ThemeCommandRepository themeCommandRepository;
    private final ThemeQueryRepository themeQueryRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final Clock clock;

    public ReservationFactory(ReservationQueryRepository reservationQueryRepository,
                              ReservationTimeCommandRepository reservationTimeCommandRepository,
                              ReservationTimeQueryRepository reservationTimeQueryRepository,
                              ThemeCommandRepository themeCommandRepository, ThemeQueryRepository themeQueryRepository, MemberQueryRepository memberQueryRepository,
                              Clock clock) {
        this.reservationQueryRepository = reservationQueryRepository;
        this.reservationTimeCommandRepository = reservationTimeCommandRepository;
        this.reservationTimeQueryRepository = reservationTimeQueryRepository;
        this.themeCommandRepository = themeCommandRepository;
        this.themeQueryRepository = themeQueryRepository;
        this.memberQueryRepository = memberQueryRepository;
        this.clock = clock;
    }

    public Reservation create(long memberId, ReservationRequest request) {
        Member member = memberQueryRepository.getById(memberId);
        Theme theme = themeQueryRepository.getById(request.themeId());
        ReservationTime reservationTime = reservationTimeQueryRepository.getById(request.timeId());
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
        if (reservationQueryRepository.existsByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId())) {
            throw new RoomescapeException(RoomescapeErrorCode.DUPLICATED_RESERVATION, "이미 존재하는 예약입니다.");
        }
    }
}
