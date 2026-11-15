package com.rishabh.ordermanagementservice.instrument;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstrumentService {

    private final InstrumentRepository repository;

    public InstrumentService( InstrumentRepository repository){
        this.repository = repository;
    }

    public List<Instrument> findAll(){
        return  repository.findAll();
    }

    public Instrument getBySymbol(String symbol){
        String normalized = symbol.trim().toUpperCase();

        return repository.findBySymbol(normalized)
                .orElseThrow(() -> new RuntimeException(("No instruemnt found with" + normalized)));
    }
}
