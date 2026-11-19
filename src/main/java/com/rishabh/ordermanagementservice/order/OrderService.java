package com.rishabh.ordermanagementservice.order;

import com.rishabh.ordermanagementservice.common.BadRequestException;
import com.rishabh.ordermanagementservice.common.NotFoundException;
import com.rishabh.ordermanagementservice.instrument.Instrument;
import com.rishabh.ordermanagementservice.instrument.InstrumentRepository;
import com.rishabh.ordermanagementservice.user.User;
import com.rishabh.ordermanagementservice.user.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;
    private final StringRedisTemplate stringRedis;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, InstrumentRepository instrumentRepository, StringRedisTemplate stringRedis) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.instrumentRepository = instrumentRepository;
        this.stringRedis = stringRedis;
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        String symbol = request.symbol().trim().toUpperCase();

        Instrument instrument = instrumentRepository.findBySymbol(symbol)
                .orElseThrow(() -> new NotFoundException("No instrument found" + symbol));

        User user = userRepository.findById(request.user_id())
                .orElseThrow(() -> new NotFoundException("No user with id: " + request.user_id()));

        String cached = stringRedis.opsForValue().get("price:" + symbol);
        BigDecimal price = (cached != null)?
                new BigDecimal(cached):
                instrument.getLast_price();

        BigDecimal cost = price.multiply(BigDecimal.valueOf(request.quantity()));

        if (request.side() == Side.BUY) {
            if (user.getCashBalance().compareTo(cost) < 0) {
                throw new BadRequestException(
                        "Insufficient funds. Need " + cost + ", have " + user.getCashBalance());
            }
            user.setCashBalance(user.getCashBalance().subtract(cost));
        } else {
            user.setCashBalance(user.getCashBalance().add(cost));
        }

        Order order = new Order(
                user.getId(),
                instrument.getId(),
                request.side(),
                request.quantity(),
                price
        );

        Order saved = orderRepository.save(order);

        return new OrderResponse(
                saved.getId(),
                instrument.getSymbol(),
                saved.getSide(),
                saved.getQuantity(),
                saved.getPrice(),
                cost,
                saved.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(order -> {
                    String symbol = instrumentRepository.findById(order.getInstrumentId())
                            .map(Instrument::getSymbol)
                            .orElse("UNKNOWN");
                    return new OrderResponse(
                            order.getId(),
                            symbol,
                            order.getSide(),
                            order.getQuantity(),
                            order.getPrice(),
                            order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())),
                            order.getStatus()
                    );
                })
                .toList();
    }
}
