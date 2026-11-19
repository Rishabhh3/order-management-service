package com.rishabh.ordermanagementservice.instrument;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/instruments")
public class InstrumentController {

    private final InstrumentService service;

    public InstrumentController(InstrumentService service){
        this.service = service;
    }

    @GetMapping
    public List<InstrumentResponse> list(){
        return service.findAll();
    }

    @GetMapping("/{symbol}")
    public InstrumentResponse get(@PathVariable String symbol){
        return service.getBySymbol(symbol);
    }

    @GetMapping("/{symbol}/price")
    public Map<String, Object>price(@PathVariable String symbol){
        return service.getLivePrice(symbol);
    }

}
