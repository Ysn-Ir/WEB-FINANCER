package com.smartfin.backend.service;

import com.smartfin.backend.model.Document;
import com.smartfin.backend.model.User;
import com.smartfin.backend.repository.DocumentRepository;
import com.smartfin.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    public Document store(MultipartFile file, String username) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Document doc = new Document();
        doc.setFilename(file.getOriginalFilename() != null ? file.getOriginalFilename()
                : "unknown_file_" + System.currentTimeMillis());
        doc.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        doc.setData(file.getBytes());
        doc.setUser(user);

        return documentRepository.save(doc);
    }

    public Document getFile(Long id, String username) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id " + id));

        if (!doc.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to file");
        }
        return doc;
    }

    public List<Document> getAllFilesForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return documentRepository.findByUserId(user.getId());
    }

    public void deleteFile(Long id, String username) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id " + id));

        // Security check
        if (!doc.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to delete this file");
        }

        documentRepository.delete(doc);
    }
}
