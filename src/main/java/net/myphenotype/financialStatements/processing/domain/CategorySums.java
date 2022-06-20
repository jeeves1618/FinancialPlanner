package net.myphenotype.financialStatements.processing.domain;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Data
@Scope(value = "prototype")
public class CategorySums {

    private double totalWithdrawals;
    private double totalDeposits;

}
