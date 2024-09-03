package com.eachnow.linebot.common.db.po;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "line_user")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LineUserPO {
    @Id
    @Column(length = 33)
    private String id;
    @Column(length = 10)
    private String name;
    @Column(updatable = false)
    @CreationTimestamp
    private Timestamp createTime;
    @Column(length = 33)
    private String notifyToken;
    @Column
    private String femasToken;
}
