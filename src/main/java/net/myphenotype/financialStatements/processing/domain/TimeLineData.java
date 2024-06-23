package net.myphenotype.financialStatements.processing.domain;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Data
public class TimeLineData {
    private LocalDate transactionDateHigh;
    private LocalDate transactionDateLow;
    private int daysBetween;
    private int monthsBetween;
}
