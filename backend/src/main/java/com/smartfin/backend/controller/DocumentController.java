package com.smartfin.backend.controller;

import com.smartfin.backend.model.Document;
import com.smartfin.backend.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file,
            java.security.Principal principal) {
        System.out.println("Received upload request for file: " + (file != null ? file.getOriginalFilename() : "null"));
        Map<String, String> response = new HashMap<>();
        try {
            if (principal == null) {
                System.err.println("Principal is null in upload!");
                response.put("error", "Unauthorized: No user logged in.");
                return ResponseEntity.status(401).body(response);
            }
            documentService.store(file, principal.getName());

            response.put("message", "File uploaded successfully: " + file.getOriginalFilename());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in uploadFile:");
            e.printStackTrace();
            response.put("error", "Could not upload the file: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getListFiles(java.security.Principal principal) {
        List<DocumentResponse> files = documentService.getAllFilesForUser(principal.getName()).stream().map(dbFile -> {
            String fileDownloadUri = "http://localhost:8080/api/documents/" + dbFile.getId();
            return new DocumentResponse(
                    dbFile.getId(),
                    dbFile.getFilename(),
                    fileDownloadUri,
                    dbFile.getContentType(),
                    dbFile.getData().length);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(files);
    }

    @Autowired
    private com.smartfin.backend.service.DataImportService dataImportService;

    @PostMapping("/import/transactions")
    public ResponseEntity<String> importTransactions(@RequestParam("file") MultipartFile file,
            java.security.Principal principal) {
        try {
            dataImportService.importTransactions(file, principal.getName());
            return ResponseEntity.ok("Transactions imported successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error importing transactions: " + e.getMessage());
        }
    }

    @PostMapping("/import/assets")
    public ResponseEntity<String> importAssets(@RequestParam("file") MultipartFile file,
            java.security.Principal principal) {
        try {
            dataImportService.importAssets(file, principal.getName());
            return ResponseEntity.ok("Assets imported successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error importing assets: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id, java.security.Principal principal) {
        Document document = documentService.getFile(id, principal.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, document.getContentType())
                .body(document.getData());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable Long id, java.security.Principal principal) {
        Map<String, String> response = new HashMap<>();
        try {
            documentService.deleteFile(id, principal.getName());
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Simple DTO for list view
    static class DocumentResponse {
        public Long id;
        public String name;
        public String url;
        public String type;
        public long size;

        public DocumentResponse(Long id, String name, String url, String type, long size) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.type = type;
            this.size = size;
        }
    }
}
