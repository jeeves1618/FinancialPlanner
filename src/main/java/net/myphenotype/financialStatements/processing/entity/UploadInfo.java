package net.myphenotype.financialStatements.processing.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Data
@Entity
@Component
@Table (name = "file_service")
public class UploadInfo {

    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Column (name = "file_upload_key")
    private Integer fileUploadKey;
    @Column (name = "file_name")
    private String fileName;
    @Column (name = "file_type")
    private String fileType;
    @Column (name = "containing_folder")
    private String containingFolder;
    @Column (name = "file_size")
    private double fileSize;
    @Column (name = "file_size_fmtd")
    private String fileSizeFmtd;
    @Column (name = "upload_date")
    private String uploadDate;
    @Column (name = "upload_time")
    private String uploadTime;
    @Column (name = "uploaded_by")
    private String uploadedBy;
    @Column (name = "upload_status")
    private String uploadStatus;

    public UploadInfo() {
    }
}

