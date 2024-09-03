package com.eachnow.linebot.common.db.po;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "remind")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RemindPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 33)
    private String userId;
    @Column(length = 30)
    private String label;
    @Column(length = 20)
    private String cron;
    @Column(length = 2)
    private Integer type;
    @Column(length = 2)
    private Integer valid;
    @Column(updatable = false)
    @CreationTimestamp
    private Timestamp createTime;
}
