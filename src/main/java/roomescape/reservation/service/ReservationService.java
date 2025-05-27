package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.AlreadyExistException;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.reservation.controller.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.controller.dto.CreateReservationWebRequest;
import roomescape.reservation.controller.dto.CreateReservationWithMemberIdWebRequest;
import roomescape.reservation.controller.dto.ReservationSearchWebRequest;
import roomescape.reservation.controller.dto.ReservationWebResponse;
import roomescape.reservation.controller.dto.ReservationWithStatusResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.service.converter.ReservationConverter;
import roomescape.reservation.service.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.service.dto.CreateReservationServiceRequest;
import roomescape.reservation.service.dto.CreateReservationWithMemberIdServiceRequest;
import roomescape.reservation.service.usecase.ReservationCommandUseCase;
import roomescape.reservation.service.usecase.ReservationQueryUseCase;
import roomescape.reservation.service.usecase.WaitingCommandUseCase;
import roomescape.reservation.service.usecase.WaitingQueryUseCase;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationQueryUseCase reservationQueryUseCase;
    private final ReservationCommandUseCase reservationCommandUseCase;
    private final WaitingCommandUseCase waitingCommandUseCase;
    private final WaitingQueryUseCase waitingQueryUseCase;

    public List<ReservationWebResponse> getAll() {
        return ReservationConverter.toDto(
                reservationQueryUseCase.getAll());
    }

    public List<ReservationWithStatusResponse> getByMemberId(final Long memberId) {
        List<ReservationWithStatusResponse> reservationWithStatusResponses = reservationQueryUseCase.getByMemberId(
                        memberId).stream()
                .map(ReservationConverter::toDtoWithStatus)
                .collect(Collectors.toList());

        waitingQueryUseCase.getWaitingWithRank(memberId).stream()
                .map(ReservationConverter::toDtoWithStatus)
                .forEach(reservationWithStatusResponses::add);

        return reservationWithStatusResponses;
    }

    public List<ReservationWebResponse> getAllWaiting() {
        return waitingQueryUseCase.getAll().stream()
                .map(ReservationConverter::toDto)
                .toList();
    }

    public List<AvailableReservationTimeWebResponse> getAvailable(final LocalDate date, final Long id) {
        final AvailableReservationTimeServiceRequest serviceRequest = new AvailableReservationTimeServiceRequest(
                date,
                id);

        return reservationQueryUseCase.getTimesWithAvailability(serviceRequest).stream()
                .map(ReservationConverter::toWebDto)
                .toList();
    }

    public ReservationWebResponse create(
            final CreateReservationWithMemberIdWebRequest createReservationWithMemberIdWebRequest) {
        return ReservationConverter.toDto(
                reservationCommandUseCase.create(
                        new CreateReservationServiceRequest(
                                createReservationWithMemberIdWebRequest.memberId(),
                                createReservationWithMemberIdWebRequest.date(),
                                createReservationWithMemberIdWebRequest.timeId(),
                                createReservationWithMemberIdWebRequest.themeId())));
    }

    public ReservationWithStatusResponse create(final CreateReservationWebRequest createReservationWebRequest,
                                                final MemberInfo memberInfo) {

        if (reservationQueryUseCase.existsByParams(
                ReservationDate.from(createReservationWebRequest.date()),
                createReservationWebRequest.timeId(),
                createReservationWebRequest.themeId())
        ) {

            validateExistOwnReservation(createReservationWebRequest, memberInfo);

            return createWaiting(createReservationWebRequest, memberInfo);
        }

        return ReservationConverter.toDtoWithStatus(
                reservationCommandUseCase.create(
                        new CreateReservationServiceRequest(
                                memberInfo.id(),
                                createReservationWebRequest.date(),
                                createReservationWebRequest.timeId(),
                                createReservationWebRequest.themeId()))
        );
    }

    @Transactional
    public void delete(final Long id) {
        final Reservation reservation = reservationQueryUseCase.get(id);
        reservationCommandUseCase.delete(id);

        if (waitingQueryUseCase.existsByParams(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        )) {
            promoteWaitingToReservation(reservation);
        }
    }

    public void deleteWaiting(Long id) {
        waitingCommandUseCase.delete(id);
    }

    public List<ReservationWebResponse> search(final ReservationSearchWebRequest reservationSearchWebRequest) {
        return reservationQueryUseCase.search(
                        reservationSearchWebRequest.memberId(),
                        reservationSearchWebRequest.themeId(),
                        ReservationDate.from(reservationSearchWebRequest.from()),
                        ReservationDate.from(reservationSearchWebRequest.to()))
                .stream()
                .map(ReservationConverter::toDto)
                .toList();
    }

    private ReservationWithStatusResponse createWaiting(
            final CreateReservationWebRequest createReservationWebRequest,
            final MemberInfo memberInfo
    ) {

        return ReservationConverter.toDtoWithStatus(
                waitingCommandUseCase.create(
                        new CreateReservationWithMemberIdServiceRequest(
                                memberInfo.id(),
                                createReservationWebRequest.date(),
                                createReservationWebRequest.timeId(),
                                createReservationWebRequest.themeId()
                        )
                )
        );
    }

    private void validateExistOwnReservation(final CreateReservationWebRequest createReservationWebRequest,
                                             final MemberInfo memberInfo) {
        if (reservationQueryUseCase.existsByParams(
                ReservationDate.from(createReservationWebRequest.date()),
                createReservationWebRequest.timeId(),
                createReservationWebRequest.themeId(),
                memberInfo.id())) {
            throw new AlreadyExistException("이미 예약이 존재합니다.");
        }
    }

    private void promoteWaitingToReservation(final Reservation reservation) {
        final Waiting waiting = waitingQueryUseCase.getEarliest(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        );

        reservationCommandUseCase.create(
                new CreateReservationServiceRequest(
                        waiting.getMember().getId(),
                        waiting.getDate().getValue(),
                        waiting.getTime().getId(),
                        waiting.getTheme().getId()
                )
        );

        waitingCommandUseCase.delete(waiting.getId());
    }
}
