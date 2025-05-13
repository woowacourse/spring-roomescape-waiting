package roomescape.reservation.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberId;
import roomescape.member.service.usecase.MemberQueryUseCase;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.converter.ReservationConverter;
import roomescape.reservation.service.dto.CreateReservationServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.service.usecase.ThemeQueryUseCase;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.time.service.usecase.ReservationTimeQueryUseCase;

@Service
@RequiredArgsConstructor
public class ReservationCommandUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationQueryUseCase reservationQueryUseCase;
    private final ReservationTimeQueryUseCase reservationTimeQueryUseCase;
    private final ThemeQueryUseCase themeQueryUseCase;
    private final MemberQueryUseCase memberQueryUseCase;

    public Reservation create(final CreateReservationServiceRequest createReservationServiceRequest) {
        if (reservationQueryUseCase.existsByParams(
                ReservationDate.from(createReservationServiceRequest.date()),
                ReservationTimeId.from(createReservationServiceRequest.timeId()),
                ThemeId.from(createReservationServiceRequest.themeId()))) {

            throw new ConflictException("추가하려는 예약이 이미 존재합니다.");
        }

        final ReservationTime reservationTime = reservationTimeQueryUseCase.get(
                ReservationTimeId.from(createReservationServiceRequest.timeId()));

        final Theme theme = themeQueryUseCase.get(
                ThemeId.from(createReservationServiceRequest.themeId()));

        final Member member = memberQueryUseCase.get(
                MemberId.from(createReservationServiceRequest.memberId()));

        return reservationRepository.save(
                ReservationConverter.toDomain(createReservationServiceRequest, member, reservationTime, theme));
    }

    public void delete(final ReservationId id) {
        reservationRepository.deleteById(id);
    }
}
