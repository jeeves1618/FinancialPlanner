package net.myphenotype.financialStatements.processing.repo;

import net.myphenotype.financialStatements.processing.entity.NlpEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NlpEntriesRepo extends JpaRepository<NlpEntry,Integer> {
}
