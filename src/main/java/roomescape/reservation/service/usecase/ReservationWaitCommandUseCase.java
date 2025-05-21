package roomescape.reservation.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.service.usecase.MemberQueryUseCase;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.repository.ReservationWaitRepository;
import roomescape.reservation.service.converter.ReservationWaitConverter;
import roomescape.reservation.service.dto.CreateReservationServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.usecase.ThemeQueryUseCase;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.usecase.ReservationTimeQueryUseCase;

@Service
@RequiredArgsConstructor
public class ReservationWaitCommandUseCase {

    private final ReservationWaitRepository reservationWaitRepository;
    private final ReservationQueryUseCase reservationQueryUseCase;
    private final ReservationTimeQueryUseCase reservationTimeQueryUseCase;
    private final ThemeQueryUseCase themeQueryUseCase;
    private final MemberQueryUseCase memberQueryUseCase;

    public ReservationWait create(final CreateReservationServiceRequest createReservationServiceRequest) {
        validateReservationExists(createReservationServiceRequest);
        validateReservationWaitNotExistsForMember(createReservationServiceRequest);

        final ReservationTime reservationTime = reservationTimeQueryUseCase.get(
                createReservationServiceRequest.timeId()
        );
        final Theme theme = themeQueryUseCase.get(createReservationServiceRequest.themeId());
        final Member member = memberQueryUseCase.get(createReservationServiceRequest.memberId());

        return reservationWaitRepository.save(
                ReservationWaitConverter.toDomain(
                        createReservationServiceRequest,
                        member,
                        reservationTime,
                        theme
                )
        );
    }

    private void validateReservationExists(final CreateReservationServiceRequest createReservationServiceRequest) {
        final Reservation reservation = reservationQueryUseCase.getByParams(
                ReservationDate.from(createReservationServiceRequest.date()),
                createReservationServiceRequest.timeId(),
                createReservationServiceRequest.themeId()
        );
        if (reservation.getMember().getId() == createReservationServiceRequest.memberId()) {
            throw new ConflictException("이미 해당 예약을 한 사용자입니다.");
        }
    }

    private void validateReservationWaitNotExistsForMember(
            final CreateReservationServiceRequest createReservationServiceRequest
    ) {
        if (reservationWaitRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                ReservationDate.from(createReservationServiceRequest.date()),
                createReservationServiceRequest.timeId(),
                createReservationServiceRequest.themeId(),
                createReservationServiceRequest.memberId()
        )) {
            throw new ConflictException("이미 해당 예약 대기를 한 사용자입니다.");
        }
    }
}
