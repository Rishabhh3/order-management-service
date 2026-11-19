package com.rishabh.ordermanagementservice.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service){
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody PlaceOrderRequest request){
        return service.placeOrder(request);
    }

    @GetMapping
    public List<OrderResponse> listByUser(@RequestParam Long userId){
        return service.listByUser(userId);
    }
}
