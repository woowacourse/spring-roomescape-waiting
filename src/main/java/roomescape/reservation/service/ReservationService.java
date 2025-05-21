package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.reservation.controller.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.controller.dto.CreateReservationByAdminWebRequest;
import roomescape.reservation.controller.dto.CreateReservationWebRequest;
import roomescape.reservation.controller.dto.ReservationSearchWebRequest;
import roomescape.reservation.controller.dto.ReservationWebResponse;
import roomescape.reservation.controller.dto.ReservationWithStatusResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.service.converter.ReservationConverter;
import roomescape.reservation.service.converter.ReservationWaitConverter;
import roomescape.reservation.service.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.service.dto.CreateReservationServiceRequest;
import roomescape.reservation.service.usecase.ReservationCommandUseCase;
import roomescape.reservation.service.usecase.ReservationQueryUseCase;
import roomescape.reservation.service.usecase.ReservationWaitCommandUseCase;
import roomescape.reservation.service.usecase.ReservationWaitQueryUseCase;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationQueryUseCase reservationQueryUseCase;
    private final ReservationCommandUseCase reservationCommandUseCase;
    private final ReservationWaitCommandUseCase reservationWaitCommandUseCase;
    private final ReservationWaitQueryUseCase reservationWaitQueryUseCase;

    public List<ReservationWebResponse> getAll() {
        return ReservationConverter.toDto(
                reservationQueryUseCase.getAll());
    }

    public List<ReservationWithStatusResponse> getWithReservationWaitByMemberId(final Long memberId) {
        final List<ReservationWithStatusResponse> allReservations = new ArrayList<>();
        allReservations.addAll(getByMemberId(memberId));
        allReservations.addAll(getReservationWaitByMemberId(memberId));

        return allReservations.stream()
                .sorted(Comparator.comparing(ReservationWithStatusResponse::getDate)
                        .thenComparing(ReservationWithStatusResponse::getTime))
                .toList();
    }

    public List<ReservationWithStatusResponse> getByMemberId(final Long memberId) {
        return reservationQueryUseCase.getByMemberId(memberId).stream()
                .map(ReservationWithStatusResponse::from)
                .toList();
    }

    public List<ReservationWithStatusResponse> getReservationWaitByMemberId(final Long memberId) {
        return reservationWaitQueryUseCase.getByMemberId(memberId).stream()
                .map(reservationWaitWithRank -> ReservationWithStatusResponse.of(
                        reservationWaitWithRank.reservationWait(),
                        reservationWaitWithRank.rank()))
                .toList();
    }

    public List<AvailableReservationTimeWebResponse> getAvailable(
            final LocalDate date,
            final Long id
    ) {
        final AvailableReservationTimeServiceRequest serviceRequest = new AvailableReservationTimeServiceRequest(date,
                id);

        return reservationQueryUseCase.getTimesWithAvailability(serviceRequest).stream()
                .map(ReservationConverter::toWebDto)
                .toList();
    }

    public ReservationWebResponse create(
            final CreateReservationByAdminWebRequest createReservationByAdminWebRequest
    ) {
        final Reservation reservation = reservationCommandUseCase.create(
                new CreateReservationServiceRequest(
                        createReservationByAdminWebRequest.memberId(),
                        createReservationByAdminWebRequest.date(),
                        createReservationByAdminWebRequest.timeId(),
                        createReservationByAdminWebRequest.themeId()
                )
        );

        return ReservationConverter.toDto(reservation);
    }

    public ReservationWebResponse create(
            final CreateReservationWebRequest createReservationWebRequest,
            final MemberInfo memberInfo
    ) {
        final Reservation reservation = reservationCommandUseCase.create(
                new CreateReservationServiceRequest(
                        memberInfo.id(),
                        createReservationWebRequest.date(),
                        createReservationWebRequest.timeId(),
                        createReservationWebRequest.themeId()
                )
        );

        return ReservationConverter.toDto(reservation);
    }

    public ReservationWebResponse createReservationWait(
            final CreateReservationWebRequest createReservationWebRequest,
            final MemberInfo memberInfo
    ) {
        final ReservationWait reservationWait = reservationWaitCommandUseCase.create(
                new CreateReservationServiceRequest(
                        memberInfo.id(),
                        createReservationWebRequest.date(),
                        createReservationWebRequest.timeId(),
                        createReservationWebRequest.themeId()
                )
        );

        return ReservationWaitConverter.toDto(reservationWait);
    }

    public void delete(final Long id) {
        reservationCommandUseCase.delete(id);
    }

    public void deleteReservationWait(final Long id) {
        reservationWaitCommandUseCase.delete(id);
    }

    public List<ReservationWebResponse> search(
            final ReservationSearchWebRequest reservationSearchWebRequest
    ) {
        final List<Reservation> reservations = reservationQueryUseCase.search(
                reservationSearchWebRequest.memberId(),
                reservationSearchWebRequest.themeId(),
                ReservationDate.from(reservationSearchWebRequest.from()),
                ReservationDate.from(reservationSearchWebRequest.to()));

        return reservations.stream()
                .map(ReservationConverter::toDto)
                .toList();
    }
}
