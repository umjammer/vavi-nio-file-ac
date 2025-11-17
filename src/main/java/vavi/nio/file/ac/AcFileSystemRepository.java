/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.ac;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.github.fge.filesystem.driver.FileSystemDriver;
import com.github.fge.filesystem.provider.FileSystemRepositoryBase;
import com.webcodepro.applecommander.storage.DiskFactory;
import com.webcodepro.applecommander.storage.Disks;
import com.webcodepro.applecommander.storage.FormattedDisk;
import org.applecommander.source.Source;
import org.applecommander.source.Sources;

import static java.lang.System.getLogger;


/**
 * AcFileSystemRepository.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2025/11/16 umjammer initial version <br>
 */
@ParametersAreNonnullByDefault
public final class AcFileSystemRepository extends FileSystemRepositoryBase {

    private static final Logger logger = getLogger(AcFileSystemRepository.class.getName());

    public AcFileSystemRepository() {
        super("ac", new AcFileSystemFactoryProvider());
    }

    /**
     * @param uri "ac:file:///foo/bar.buz"
     */
    @Nonnull
    @Override
    public FileSystemDriver createDriver(URI uri, Map<String, ?> env) throws IOException {
        String[] rawSchemeSpecificParts = uri.getRawSchemeSpecificPart().split("!");
        URI file = URI.create(rawSchemeSpecificParts[0]);
        if (!"file".equals(file.getScheme())) {
            // currently only support "file"
            throw new IllegalArgumentException(file.toString());
        }
        if (!file.getRawSchemeSpecificPart().startsWith("/")) {
            file = URI.create(file.getScheme() + ":" + System.getProperty("user.dir") + "/" + file.getRawSchemeSpecificPart());
        }

logger.log(Level.DEBUG, "path: " + file);
        Source source = Sources.create(Path.of(file).toFile()).orElseThrow();
        DiskFactory.Context context = Disks.inspect(source);
        if (context.disks.isEmpty()) throw new IllegalArgumentException(uri.toString());
        FormattedDisk disk = context.disks.get(0); // TODO index property
logger.log(Level.DEBUG, "disk: " + disk.getFormat());

        AcFileStore fileStore = new AcFileStore(disk, factoryProvider.getAttributesFactory());
        return new AcFileSystemDriver(fileStore, factoryProvider, disk, env);
    }

    /* ad-hoc hack for ignoring checking opacity */
    @Override
    protected void checkURI(@Nullable URI uri) {
        Objects.requireNonNull(uri);
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("uri is not absolute");
        }
        if (!getScheme().equals(uri.getScheme())) {
            throw new IllegalArgumentException("bad scheme");
        }
    }
}
