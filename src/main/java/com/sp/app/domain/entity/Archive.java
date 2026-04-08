package com.sp.app.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "ARCHIVES",
       uniqueConstraints = @UniqueConstraint(columnNames = {"POST_ID", "LOGIN_ID"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Archive {
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "archives_seq")
    @SequenceGenerator(name = "archives_seq", sequenceName = "ARCHIVES_SEQ", allocationSize = 1)
    @Column(name = "ARCHIVE_ID")
    private Long archiveId;
 
    @Column(name = "POST_ID", nullable = false)
    private Long postId;
 
    @Column(name = "LOGIN_ID", nullable = false, length = 50)
    private String loginId;
 
    @CreationTimestamp
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}
