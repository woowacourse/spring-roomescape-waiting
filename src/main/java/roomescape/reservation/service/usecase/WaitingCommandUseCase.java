package roomescape.reservation.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.AlreadyExistException;
import roomescape.member.domain.Member;
import roomescape.member.service.usecase.MemberQueryUseCase;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.reservation.service.dto.CreateReservationWithMemberIdServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.usecase.ThemeQueryUseCase;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.usecase.ReservationTimeQueryUseCase;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingCommandUseCase {

    private final WaitingRepository waitingRepository;
    private final WaitingQueryUseCase waitingQueryUseCase;
    private final ReservationTimeQueryUseCase reservationTimeQueryUseCase;
    private final ThemeQueryUseCase themeQueryUseCase;
    private final MemberQueryUseCase memberQueryUseCase;

    public Waiting create(final CreateReservationWithMemberIdServiceRequest createReservationServiceRequest) {
        validateExistWaiting(createReservationServiceRequest);

        final ReservationTime reservationTime = reservationTimeQueryUseCase.get(
                createReservationServiceRequest.timeId());

        final Theme theme = themeQueryUseCase.get(createReservationServiceRequest.themeId());

        final Member member = memberQueryUseCase.get(createReservationServiceRequest.memberId());

        return waitingRepository.save(
                Waiting.withoutId(
                        member,
                        ReservationDate.from(createReservationServiceRequest.date()),
                        reservationTime,
                        theme
                )
        );
    }

    public void delete(Long id) {
        waitingRepository.deleteById(id);
    }

    private void validateExistWaiting(CreateReservationWithMemberIdServiceRequest createReservationServiceRequest) {
        if (waitingQueryUseCase.existsByParams(
                ReservationDate.from(createReservationServiceRequest.date()),
                createReservationServiceRequest.timeId(),
                createReservationServiceRequest.themeId(),
                createReservationServiceRequest.memberId())
        ) {
            throw new AlreadyExistException("추가하려는 예약 대기 정보가 이미 존재합니다.");
        }
    }
}
