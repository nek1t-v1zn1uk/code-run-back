package com.example.coderun.domain.users

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class FileStorageService {

    @Value("\${app.upload-dir}")
    private lateinit var uploadDir: String

    private lateinit var avatarsLocation: Path

    @PostConstruct
    fun init() {
        avatarsLocation = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize()
        Files.createDirectories(avatarsLocation)
    }

    fun storeAvatar(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: ""
        val extension = originalFilename.substringAfterLast('.', "")
        val allowedExtensions = listOf("jpg", "jpeg", "png", "gif", "webp")

        if (extension.lowercase() !in allowedExtensions) {
            throw IllegalArgumentException("Invalid file extension. Allowed extensions are: \${allowedExtensions.joinToString()}")
        }

        val filename = "\${UUID.randomUUID()}.\$extension"
        val targetLocation = avatarsLocation.resolve(filename)

        Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

        return "avatars/\$filename"
    }

    fun deleteAvatar(relativePath: String) {
        if (relativePath.startsWith("avatars/")) {
            val filename = relativePath.substringAfterLast("/")
            val fileLocation = avatarsLocation.resolve(filename)
            try {
                Files.deleteIfExists(fileLocation)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
