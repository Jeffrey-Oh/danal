package com.danal.batch.domain.errorLog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "error_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long errorLogId;

    @Column(columnDefinition = "TEXT")
    private String message;

    public ErrorLog(String message) {
        this.message = message;
    }

}
