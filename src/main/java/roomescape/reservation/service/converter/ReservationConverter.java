package roomescape.reservation.service.converter;

import java.util.List;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberConverter;
import roomescape.reservation.controller.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.controller.dto.ReservationStatus;
import roomescape.reservation.controller.dto.ReservationWebResponse;
import roomescape.reservation.controller.dto.ReservationWithStatusResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.dto.WaitingWithRankDto;
import roomescape.reservation.service.dto.AvailableReservationTimeServiceResponse;
import roomescape.reservation.service.dto.CreateReservationServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.converter.ThemeConverter;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.converter.ReservationTimeConverter;

public class ReservationConverter {

    public static Reservation toDomain(final CreateReservationServiceRequest request,
                                       final Member member,
                                       final ReservationTime time,
                                       final Theme theme) {
        return Reservation.withoutId(
                member,
                ReservationDate.from(request.date()),
                time,
                theme);
    }

    public static ReservationWebResponse toDto(final Reservation reservation) {
        return new ReservationWebResponse(
                reservation.getId(),
                MemberConverter.toDto(reservation.getMember()),
                reservation.getDate().getValue(),
                ReservationTimeConverter.toDto(reservation.getTime()),
                ThemeConverter.toDto(reservation.getTheme()));
    }

    public static ReservationWebResponse toDto(final Waiting waiting) {
        return new ReservationWebResponse(
                waiting.getId(),
                MemberConverter.toDto(waiting.getMember()),
                waiting.getDate().getValue(),
                ReservationTimeConverter.toDto(waiting.getTime()),
                ThemeConverter.toDto(waiting.getTheme()));
    }

    public static List<ReservationWebResponse> toDto(final List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationConverter::toDto)
                .toList();
    }

    public static AvailableReservationTimeWebResponse toWebDto(
            final AvailableReservationTimeServiceResponse availableReservationTimeServiceResponse) {
        return new AvailableReservationTimeWebResponse(
                availableReservationTimeServiceResponse.startAt(),
                availableReservationTimeServiceResponse.timeId(),
                availableReservationTimeServiceResponse.isBooked()
        );
    }

    public static ReservationWithStatusResponse toDtoWithStatus(Waiting waiting) {
        return new ReservationWithStatusResponse(
                waiting.getId(),
                waiting.getTheme().getName().getValue(),
                waiting.getDate().getValue(),
                waiting.getTime().getStartAt(),
                ReservationStatus.WAITING.getStatus()
        );
    }

    public static ReservationWithStatusResponse toDtoWithStatus(WaitingWithRankDto waitingWithRank) {
        return new ReservationWithStatusResponse(
                waitingWithRank.id(),
                waitingWithRank.themeName(),
                waitingWithRank.date().toLocalDate(),
                waitingWithRank.time().toLocalTime(),
                waitingWithRank.rank().intValue()
        );
    }

    public static ReservationWithStatusResponse toDtoWithStatus(Reservation reservation) {
        return new ReservationWithStatusResponse(
                reservation.getId(),
                reservation.getTheme().getName().getValue(),
                reservation.getDate().getValue(),
                reservation.getTime().getStartAt(),
                ReservationStatus.CONFIRM.getStatus()
        );
    }
}
