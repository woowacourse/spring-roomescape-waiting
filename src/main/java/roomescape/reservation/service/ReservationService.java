package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.dto.CreateReservationWithMemberRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse createReservation(final CreateReservationWithMemberRequest request) {
        final Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new BadRequestException("예약자를 찾을 수 없습니다."));
        final Reservation reservation = convertToReservation(request, member);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    public List<ReservationResponse> getReservations() {
        final List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> getReservations(
            final Long memberId,
            final Long themeId,
            final LocalDate dateFrom,
            final LocalDate dateTo
    ) {
        final List<Reservation> reservations = reservationRepository.findAll(memberId, themeId, dateFrom, dateTo);
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public void cancelReservationById(final long id) {
        reservationRepository.deleteById(id);
    }

    private Reservation convertToReservation(
            final CreateReservationWithMemberRequest reservationRequest,
            final Member member
    ) {
        return convertToReservation(
                member,
                reservationRequest.themeId(),
                reservationRequest.timeId(),
                reservationRequest.date());
    }

    private Reservation convertToReservation(
            final Member member,
            final long themeId,
            final long timeId,
            final LocalDate date
    ) {
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new BadRequestException("테마가 존재하지 않습니다."));
        final ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BadRequestException("예약 시간이 존재하지 않습니다."));
        final ReservationDateTime dateTime = new ReservationDateTime(date, time);
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("지나간 날짜와 시간은 예약 불가합니다.");
        }
        if (reservationRepository.existsByDateAndTimeAndTheme(dateTime.getDate(), dateTime.getTimeId(),
                theme.getId())) {
            throw new BadRequestException("해당 시간에 이미 예약이 존재합니다.");
        }
        return Reservation.register(member, date, time, theme);
    }
}
