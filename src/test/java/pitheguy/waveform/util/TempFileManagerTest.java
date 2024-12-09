package pitheguy.waveform.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TempFileManagerTest {
    @AfterEach
    void tearDown() {
        TempFileManager.cleanupTempFiles();
    }

    @Test
    void testCreateTempFile() throws IOException {
        File tempFile = TempFileManager.createTempFile("test", ".test");
        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());
        assertTrue(TempFileManager.getTempFiles().contains(tempFile));
    }

    @Test
    void testRegisterTempFile() {
        File file = new File("Test");
        TempFileManager.registerTempFile(file);
        assertTrue(TempFileManager.getTempFiles().contains(file));
    }

    @Test
    void testDeleteTempFile() throws IOException {
        File tempFile = TempFileManager.createTempFile("test", ".test");
        TempFileManager.deleteTempFile(tempFile);
        assertFalse(TempFileManager.getTempFiles().contains(tempFile));
        assertFalse(tempFile.exists());
    }

    @Test
    void testDeleteTempFile_nonExistentFile() {
        File file = new File("Test");
        TempFileManager.registerTempFile(file);
        assertDoesNotThrow(() -> TempFileManager.deleteTempFile(file));
    }

    @Test
    void testCleanupTempFiles() throws IOException {
        List<File> tempFiles = List.of(
                TempFileManager.createTempFile("test", ".test"),
                TempFileManager.createTempFile("test2", ".test"),
                TempFileManager.createTempFile("test3", ".test")
        );
        TempFileManager.cleanupTempFiles();
        assertTrue(tempFiles.stream().noneMatch(File::exists));
    }

}