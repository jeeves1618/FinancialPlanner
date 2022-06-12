package net.myphenotype.financialStatements.processing.domain;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class JournalRel {

    private Integer journalKey;
    private String journalMessage;
}
