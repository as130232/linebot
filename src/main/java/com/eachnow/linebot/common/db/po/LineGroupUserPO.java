package com.eachnow.linebot.common.db.po;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "line_group_user")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LineGroupUserPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 33)
    private String userId;
    @Column(length = 33)
    private String groupId;
    @Column(updatable = false)
    @CreationTimestamp
    private Timestamp createTime;
}
