/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.ac;

import com.github.fge.filesystem.provider.FileSystemProviderBase;


/**
 * AcFileSystemProvider.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2025/11/16 umjammer initial version <br>
 */
public final class AcFileSystemProvider extends FileSystemProviderBase {

    public static final String PARAM_ALIAS = "alias";

    public AcFileSystemProvider() {
        super(new AcFileSystemRepository());
    }
}
