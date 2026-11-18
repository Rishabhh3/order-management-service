package com.rishabh.ordermanagementservice.instrument;

import com.rishabh.ordermanagementservice.common.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstrumentService {

    private final InstrumentRepository repository;

    public InstrumentService( InstrumentRepository repository){
        this.repository = repository;
    }
    // controller is calling this in service and service is calling repository  and then handing results back to handler
    // now even though in repository the jpa will handle this but because controller cannot directly talk to
    // repo , so service also needs to have it, even though it will call repo for same
    // so controller calls, serice.abc and in service either write logic or repo.abc and either write logic in repo or inherit
    public List<InstrumentResponse> findAll(){
        return  repository.findAll().stream()
                .map(InstrumentResponse::from)
                .toList();
    }

    public InstrumentResponse getBySymbol(String symbol){
        String normalized = symbol.trim().toUpperCase();

        return repository.findBySymbol(normalized)
                .map(InstrumentResponse::from)
                .orElseThrow(() -> new NotFoundException("No instrument found: " + normalized));
    }
}
