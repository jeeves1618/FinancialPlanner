package net.myphenotype.financialStatements.processing.repo;

import net.myphenotype.financialStatements.processing.entity.UploadInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadServiceRepo extends JpaRepository<UploadInfo, Integer> {

}
