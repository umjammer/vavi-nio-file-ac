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
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.github.fge.filesystem.driver.FileSystemDriver;
import com.github.fge.filesystem.provider.FileSystemRepositoryBase;

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
     * @param uri "ac:protocol:///?alias=alias", sub url (after "ac:") parts will be replaced by properties.
     *            if you don't use alias, the url must include username, password, host, port.
     */
    @Nonnull
    @Override
    public FileSystemDriver createDriver(URI uri, Map<String, ?> env) throws IOException {
        String uriString = uri.toString();
        URI subUri = URI.create(uriString.substring(uriString.indexOf(':') + 1));
        String protocol = subUri.getScheme();
logger.log(Level.DEBUG, "protocol: " + protocol);

        Map<String, String> params = getParamsMap(subUri);
        String alias = params.get(AcFileSystemProvider.PARAM_ALIAS);

        AcFileStore fileStore = new AcFileStore(null, factoryProvider.getAttributesFactory());
        return new AcFileSystemDriver(fileStore, factoryProvider, env);
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
