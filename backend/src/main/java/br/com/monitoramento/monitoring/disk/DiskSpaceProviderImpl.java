package br.com.monitoramento.monitoring.disk;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lê o uso de disco do filesystem Linux utilizando {@link FileStore}.
 */
@Component
public class DiskSpaceProviderImpl implements DiskSpaceProvider {

    @Override
    public DiskSpaceInfo lerUsoDisco(String path) {
        try {
            FileStore fileStore = Files.getFileStore(Path.of(path));
            long total = fileStore.getTotalSpace();
            long livre = fileStore.getUsableSpace();
            long utilizado = total - livre;
            return new DiskSpaceInfo(total, utilizado, livre);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler informações de disco para o caminho: " + path, e);
        }
    }
}
