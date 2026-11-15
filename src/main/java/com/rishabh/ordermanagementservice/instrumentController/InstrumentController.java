package com.rishabh.ordermanagementservice.instrumentController;


import com.rishabh.ordermanagementservice.instrument.Instrument;
import com.rishabh.ordermanagementservice.instrument.InstrumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instruments")
public class InstrumentController {

    private final InstrumentService service;

    public InstrumentController(InstrumentService service){
        this.service = service;
    }

    @GetMapping
    public List<Instrument> list(){
        return service.findAll();
    }

    @GetMapping("/{symbol}")
    public Instrument get(@PathVariable String symbol){
        return service.getBySymbol(symbol);
    }

}
