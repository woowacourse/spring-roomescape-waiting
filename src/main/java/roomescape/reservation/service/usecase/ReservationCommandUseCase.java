package roomescape.reservation.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.service.usecase.MemberQueryUseCase;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.converter.ReservationConverter;
import roomescape.reservation.service.dto.CreateReservationServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.usecase.ThemeQueryUseCase;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.usecase.ReservationTimeQueryUseCase;

@Service
@RequiredArgsConstructor
public class ReservationCommandUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationQueryUseCase reservationQueryUseCase;

    private final ReservationWaitQueryUseCase reservationWaitQueryUseCase;
    private final ReservationWaitCommandUseCase reservationWaitCommandUseCase;

    private final ReservationTimeQueryUseCase reservationTimeQueryUseCase;
    private final ThemeQueryUseCase themeQueryUseCase;
    private final MemberQueryUseCase memberQueryUseCase;

    public Reservation create(final CreateReservationServiceRequest createReservationServiceRequest) {
        validateReservationNotExists(createReservationServiceRequest);

        final ReservationTime reservationTime = reservationTimeQueryUseCase.get(
                createReservationServiceRequest.timeId());

        final Theme theme = themeQueryUseCase.get(createReservationServiceRequest.themeId());

        final Member member = memberQueryUseCase.get(createReservationServiceRequest.memberId());

        return reservationRepository.save(
                ReservationConverter.toDomain(createReservationServiceRequest, member, reservationTime, theme)
        );
    }

    private void validateReservationNotExists(final CreateReservationServiceRequest createReservationServiceRequest) {
        if (reservationQueryUseCase.existsByParams(
                ReservationDate.from(createReservationServiceRequest.date()),
                createReservationServiceRequest.timeId(),
                createReservationServiceRequest.themeId())) {

            throw new ConflictException("추가하려는 예약이 이미 존재합니다.");
        }
    }

    public void delete(final Long id) {
        // id에 해당하는 예약을 조회하고, 없으면 예외가 발생한다.
        final Reservation deletedReservation = reservationQueryUseCase.get(id);

        // 예약을 삭제한다.
        reservationRepository.deleteById(id);

        // 이에 해당하는 예약 대기가 없다면 메서드를 종료하고, 존재한다면 가장 첫 번째 예약 대기를 승격시킨다.
        reservationWaitQueryUseCase.findByParamsAt(
                deletedReservation.getDate(),
                deletedReservation.getTime().getId(),
                deletedReservation.getTheme().getId(),
                0
        ).ifPresent(this::promotionReservationWait);
    }

    private void promotionReservationWait(final ReservationWait firstReservationWait) {
        // 해당 예약 대기를 예약 대기에서 삭제한다.
        reservationWaitCommandUseCase.delete(firstReservationWait.getId());

        // 해당 예약 대기를 예약으로 승격한다.
        final Reservation reservation = firstReservationWait.toReservation();

        // 해당 예약을 추가한다.
        reservationRepository.save(reservation);
    }
}
