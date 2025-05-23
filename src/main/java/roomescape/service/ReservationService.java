package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.schdule.ReservationDate;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationScheduleRepository;
import roomescape.repository.ReservationSpecifications;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.request.AdminCreateReservationRequest;
import roomescape.service.request.ReservationCreateRequest;
import roomescape.service.response.MyReservationResponse;
import roomescape.service.response.ReservationResponse;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;
    private final ReservationScheduleRepository reservationScheduleRepository;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository,
            final Clock clock,
            final ReservationScheduleRepository reservationScheduleRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
        this.reservationScheduleRepository = reservationScheduleRepository;
    }

    public List<ReservationResponse> findAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return ReservationResponse.from(reservations);
    }

    public void deleteReservationById(final Long id) {
        Reservation reservation = getReservation(id);
        reservationRepository.deleteById(reservation.getId());
    }

    public ReservationResponse createReservation(final ReservationCreateRequest request, final Long memberId) {
        Reservation created = createReservation(
                new ReservationDate(request.date()),
                request.timeId(),
                request.themeId(),
                memberId
        );
        return ReservationResponse.from(created);
    }

    public ReservationResponse createReservationByAdmin(final AdminCreateReservationRequest request) {
        Reservation created = createReservation(
                new ReservationDate(request.date()),
                request.timeId(),
                request.themeId(),
                request.memberId()
        );
        return ReservationResponse.from(created);
    }

    private Reservation createReservation(
            final ReservationDate date,
            final Long timeId,
            final Long themeId,
            final Long memberId
    ) {
        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("예약 시간을 찾을 수 없습니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("해당 테마가 존재하지 않습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 멤버가 존재하지 않습니다."));
        ReservationSchedule schedule = reservationScheduleRepository.findByReservationTime_IdAndTheme_IdAndReservationDate_Date(
                time.getId(),
                theme.getId(),
                date.date()
        ).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약 일정입니다."));
        validateReservationAvailability(schedule);
        Reservation reservation = new Reservation(null, member, schedule);
        return reservationRepository.save(reservation);
    }

    private void validateReservationAvailability(final ReservationSchedule schedule) {
        if (reservationRepository.findByScheduleId(schedule.getId()).isPresent()) {
            throw new IllegalStateException("이미 예약이 찼습니다.");
        }
    }

    private Reservation getReservation(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("예약을 찾을 수 없습니다."));
    }

    public List<ReservationResponse> findAllReservationsWithFilter(
            final Long memberId,
            final Long themeId,
            final LocalDate fromDate,
            final LocalDate toDate
    ) {
        final Specification<Reservation> spec = Specification
                .where(ReservationSpecifications.hasMemberId(memberId))
                .and(ReservationSpecifications.hasThemeId(themeId))
                .and(ReservationSpecifications.dateAfterOrEqual(fromDate))
                .and(ReservationSpecifications.dateBeforeOrEqual(toDate));

        return ReservationResponse.from(reservationRepository.findAll(spec));
    }

    public List<MyReservationResponse> findAllMyReservation(final Long id) {
        List<Reservation> reservations = reservationRepository.findByMember_Id(id);
        return MyReservationResponse.from(reservations);
    }
}
