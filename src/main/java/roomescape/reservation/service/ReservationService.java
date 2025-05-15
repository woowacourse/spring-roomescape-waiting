package roomescape.reservation.service;

import java.time.LocalDate;
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
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.service.converter.ReservationConverter;
import roomescape.reservation.service.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.service.dto.CreateReservationServiceRequest;
import roomescape.reservation.service.usecase.ReservationCommandUseCase;
import roomescape.reservation.service.usecase.ReservationQueryUseCase;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationQueryUseCase reservationQueryUseCase;
    private final ReservationCommandUseCase reservationCommandUseCase;

    public List<ReservationWebResponse> getAll() {
        return ReservationConverter.toDto(
                reservationQueryUseCase.getAll());
    }

    public List<ReservationWithStatusResponse> getByMemberId(Long memberId) {
        return reservationQueryUseCase.getByMemberId(memberId).stream()
                .map(reservation -> new ReservationWithStatusResponse(
                        reservation.getId(),
                        reservation.getTheme().getName().getValue(),
                        reservation.getDate().getValue(),
                        reservation.getTime().getStartAt(),
                        "예약"
                ))
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

    public ReservationWebResponse create(final CreateReservationByAdminWebRequest createReservationByAdminWebRequest) {
        return ReservationConverter.toDto(
                reservationCommandUseCase.create(
                        new CreateReservationServiceRequest(
                                createReservationByAdminWebRequest.memberId(),
                                createReservationByAdminWebRequest.date(),
                                createReservationByAdminWebRequest.timeId(),
                                createReservationByAdminWebRequest.themeId())));
    }

    public ReservationWebResponse create(final CreateReservationWebRequest createReservationWebRequest,
                                         final MemberInfo memberInfo) {
        return ReservationConverter.toDto(
                reservationCommandUseCase.create(
                        new CreateReservationServiceRequest(
                                memberInfo.id(),
                                createReservationWebRequest.date(),
                                createReservationWebRequest.timeId(),
                                createReservationWebRequest.themeId())));
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
