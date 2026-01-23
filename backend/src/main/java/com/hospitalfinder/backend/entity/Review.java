package com.hospitalfinder.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    private String id;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;

    // Relations stored as IDs to keep it lightweight and flexible
    private String userId;

    private String hospitalId;

    private String doctorId;
}
