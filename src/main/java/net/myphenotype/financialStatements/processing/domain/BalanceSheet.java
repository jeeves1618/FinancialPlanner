package net.myphenotype.financialStatements.processing.domain;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Data
@Scope (value = "prototype")
public class BalanceSheet {

    private String formulaString;
    private String accountTitle;
    private double balance;
    private String balanceFmtd;
}
