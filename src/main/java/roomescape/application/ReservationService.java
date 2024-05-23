package roomescape.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.request.ReservationRequest;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.exception.BadRequestException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse addReservation(ReservationRequest request) {
        Reservation reservation = createReservation(
                request.date(),
                request.memberId(),
                request.timeId(),
                request.themeId()
        );

        validateReservationExists(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> getReservationsByConditions(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationRepository.findAllByConditions(memberId, themeId, dateFrom, dateTo);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReservationById(Long id) {
        Reservation reservation = reservationRepository.getById(id);

        reservationRepository.delete(reservation);
    }

    private Reservation createReservation(LocalDate date, Long memberId, Long timeId, Long themeId) {
        Member member = memberRepository.getById(memberId);
        ReservationTime reservationTime = reservationTimeRepository.getById(timeId);
        Theme theme = themeRepository.getById(themeId);

        return Reservation.create(LocalDateTime.now(clock), date, member, reservationTime, theme);
    }

    private void validateReservationExists(Reservation reservation) {
        boolean reservationExists = reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        );

        if (reservationExists) {
            throw new BadRequestException("이미 예약이 존재합니다.");
        }
    }
}
