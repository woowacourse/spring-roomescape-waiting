package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.reservation.controller.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.controller.dto.CreateReservationByAdminWebRequest;
import roomescape.reservation.controller.dto.CreateReservationWebRequest;
import roomescape.reservation.controller.dto.ReservationSearchWebRequest;
import roomescape.reservation.controller.dto.ReservationWebResponse;
import roomescape.reservation.controller.dto.ReservationWithStatusResponse;
import roomescape.reservation.domain.ReservationDate;
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
                .map(reservation -> new ReservationWithStatusResponse(
                        reservation.getId(),
                        reservation.getTheme().getName().getValue(),
                        reservation.getDate().getValue(),
                        reservation.getTime().getStartAt(),
                        Status.CONFIRMED.getTitle()
                ))
                .toList();
    }

    public List<ReservationWithStatusResponse> getReservationWaitByMemberId(final Long memberId) {
        return reservationWaitQueryUseCase.getByMemberId(memberId).stream()
                .map(reservationWaitWithRank -> new ReservationWithStatusResponse(
                        reservationWaitWithRank.reservationWait().getId(),
                        reservationWaitWithRank.reservationWait().getTheme().getName().getValue(),
                        reservationWaitWithRank.reservationWait().getDate().getValue(),
                        reservationWaitWithRank.reservationWait().getTime().getStartAt(),
                        Status.PENDING.getTitle(),
                        reservationWaitWithRank.rank()
                ))
                .toList();
    }

    @Getter
    @RequiredArgsConstructor
    private enum Status {
        CONFIRMED("예약"),
        PENDING("예약대기");

        private final String title;
    }

    public List<AvailableReservationTimeWebResponse> getAvailable(final LocalDate date, final Long id) {
        final AvailableReservationTimeServiceRequest serviceRequest = new AvailableReservationTimeServiceRequest(
                date,
                id);

        return reservationQueryUseCase.getTimesWithAvailability(serviceRequest).stream()
                .map(ReservationConverter::toWebDto)
                .toList();
    }

    public ReservationWebResponse create(final CreateReservationByAdminWebRequest createReservationByAdminWebRequest) {
        return ReservationConverter.toDto(
                reservationCommandUseCase.create(
                        new CreateReservationServiceRequest(
                                createReservationByAdminWebRequest.memberId(),
                                createReservationByAdminWebRequest.date(),
                                createReservationByAdminWebRequest.timeId(),
                                createReservationByAdminWebRequest.themeId())));
    }

    public ReservationWebResponse create(
            final CreateReservationWebRequest createReservationWebRequest,
            final MemberInfo memberInfo
    ) {
        return ReservationConverter.toDto(
                reservationCommandUseCase.create(
                        new CreateReservationServiceRequest(
                                memberInfo.id(),
                                createReservationWebRequest.date(),
                                createReservationWebRequest.timeId(),
                                createReservationWebRequest.themeId())));
    }

    public ReservationWebResponse createReservationWait(
            final CreateReservationWebRequest createReservationWebRequest,
            final MemberInfo memberInfo
    ) {
        return ReservationWaitConverter.toDto(
                reservationWaitCommandUseCase.create(
                        new CreateReservationServiceRequest(
                                memberInfo.id(),
                                createReservationWebRequest.date(),
                                createReservationWebRequest.timeId(),
                                createReservationWebRequest.themeId()
                        )
                )
        );
    }

    public void delete(final Long id) {
        reservationCommandUseCase.delete(id);
    }

    public List<ReservationWebResponse> search(ReservationSearchWebRequest reservationSearchWebRequest) {
        return reservationQueryUseCase.search(
                        reservationSearchWebRequest.memberId(),
                        reservationSearchWebRequest.themeId(),
                        ReservationDate.from(reservationSearchWebRequest.from()),
                        ReservationDate.from(reservationSearchWebRequest.to()))
                .stream()
                .map(ReservationConverter::toDto)
                .toList();
    }
}
