package net.myphenotype.financialStatements.processing.domain;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class UIMetaData {

    private String titleText;
    private String enableButtonIndicator;
    private String typeOfStatement;
}
