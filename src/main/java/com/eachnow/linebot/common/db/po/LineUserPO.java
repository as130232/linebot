package com.eachnow.linebot.common.db.po;

import lombok.*;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
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
}
