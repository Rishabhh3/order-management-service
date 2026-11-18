package com.rishabh.ordermanagementservice.order;

import com.rishabh.ordermanagementservice.common.BadRequestException;
import com.rishabh.ordermanagementservice.common.NotFoundException;
import com.rishabh.ordermanagementservice.instrument.Instrument;
import com.rishabh.ordermanagementservice.instrument.InstrumentRepository;
import com.rishabh.ordermanagementservice.user.User;
import com.rishabh.ordermanagementservice.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;


    public OrderService(OrderRepository orderRepository, UserRepository userRepository, InstrumentRepository instrumentRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.instrumentRepository = instrumentRepository;
    }

    @Transactional
    public Order placeOrder(PlaceOrderRequest request) {
        String symbol = request.symbol().trim().toUpperCase();

        Instrument instrument = instrumentRepository.findBySymbol(symbol)
                .orElseThrow(() -> new NotFoundException("No instrument found" + symbol));

        User user = userRepository.findById(request.user_id())
                .orElseThrow(() -> new NotFoundException("No user with id: " + request.user_id()));

        BigDecimal price = instrument.getLast_price();
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

        return orderRepository.save(order);
    }
}
