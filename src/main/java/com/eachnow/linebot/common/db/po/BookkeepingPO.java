package com.eachnow.linebot.common.db.po;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "bookkeeping")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BookkeepingPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 33)
    private String userId;
    @Column(length = 30)
    private String typeName;
    @Column(length = 10)
    private BigDecimal amount;
    @Column(length = 5)
    private String currency;
    @Column(updatable = false)
    @CreationTimestamp
    private Timestamp createTime;
}
