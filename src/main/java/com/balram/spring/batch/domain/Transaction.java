package com.balram.spring.batch.domain;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "TRANSACTION")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ACCOUNT")
    private String account;

    @Column(name = "TIMESTAMP")
    private Date timestamp;

    @Column(name = "AMOUNT")
    private BigDecimal amount;
}