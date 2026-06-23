package roomescape.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.payment.model.ReservationOrderResult;
import roomescape.application.query.ReservationQueryService;
import roomescape.application.query.ReservationTimeQueryService;
import roomescape.application.query.ThemeQueryService;
import roomescape.domain.Member;
import roomescape.domain.Order;
import roomescape.domain.OrderRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.ConflictException;
import roomescape.presentation.dto.ReservationOrderRequest;

@Service
public class OrderCreateUseCase {

    private static final long DEFAULT_AMOUNT = 50_000L;
    private static final String ORDER_NAME_SUFFIX = " 예약";
    private static final String ALREADY_RESERVED_SLOT = "이미 예약된 슬롯입니다.";

    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;
    private final ReservationQueryService reservationQueryService;
    private final OrderRepository orderRepository;
    private final Clock clock;

    public OrderCreateUseCase(
            ReservationTimeQueryService reservationTimeQueryService,
            ThemeQueryService themeQueryService,
            ReservationQueryService reservationQueryService,
            OrderRepository orderRepository,
            Clock clock
    ) {
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.themeQueryService = themeQueryService;
        this.reservationQueryService = reservationQueryService;
        this.orderRepository = orderRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationOrderResult create(ReservationOrderRequest request) {
        ReservationTime reservationTime = reservationTimeQueryService.getById(request.timeId());
        Theme theme = themeQueryService.getById(request.themeId());
        Slot slot = new Slot(
                request.date(),
                reservationTime,
                theme
        );
        Reservation reservation = Reservation.createWith(
                new Member(request.name()),
                slot,
                LocalDateTime.now(clock)
        );

        validateAvailable(slot);

        Order order = new Order(
                generateOrderId(),
                theme.getName() + ORDER_NAME_SUFFIX,
                DEFAULT_AMOUNT,
                reservation
        );
        orderRepository.save(order);

        return new ReservationOrderResult(
                order.getOrderId(),
                order.getOrderName(),
                order.getAmount()
        );
    }

    @Transactional(readOnly = true)
    public ReservationOrderResult getByOrderId(String orderId) {
        Order order = orderRepository.getByOrderId(orderId);
        return new ReservationOrderResult(
                order.getOrderId(),
                order.getOrderName(),
                order.getAmount()
        );
    }

    private void validateAvailable(Slot slot) {
        reservationQueryService.findBySlot(slot)
                .ifPresent(reservation -> {
                    throw new ConflictException(ALREADY_RESERVED_SLOT);
                });
    }

    private String generateOrderId() {
        return "order-" + UUID.randomUUID();
    }
}
