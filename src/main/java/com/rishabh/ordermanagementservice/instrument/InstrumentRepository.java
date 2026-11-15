package com.rishabh.ordermanagementservice.instrument;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// handles Instrument object whose id is Long, just by inheriting we get findall , delete al .....
public interface  InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findBySymbol(String symbol);

    // findBySymbol is the interesting part. Spring reads the method name, sees findBy + Symbol,
    // matches Symbol to a field on your entity, and generates SELECT * FROM instruments WHERE symbol = ?.
    // You wrote a name and got a query. findByExchange, findBySymbolAndExchange would work the same way.
    //Optional is there because the symbol might not exist. It forces whoever calls this to handle the “nothing found” case
    // instead of getting a surprise null.
}
