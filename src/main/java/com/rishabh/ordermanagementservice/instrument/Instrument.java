package com.rishabh.ordermanagementservice.instrument;

import jakarta.persistence.*;

import javax.annotation.processing.Generated;
import java.math.BigDecimal;

@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String Exchange;

    @Column(name = "last_price", nullable = false)
    private BigDecimal last_price;

    protected  Instrument(){

    }

    public Long getId(){ return id;}
    public String getSymbol(){return symbol;}
    public String getName(){return name;}
    public String getExchange(){return Exchange;}
    public BigDecimal getLast_price(){return last_price;}
}
