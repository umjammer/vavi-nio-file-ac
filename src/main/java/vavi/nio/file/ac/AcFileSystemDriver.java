/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.ac;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.CopyOption;
import java.nio.file.FileStore;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;

import com.github.fge.filesystem.driver.ExtendedFileSystemDriver;
import com.github.fge.filesystem.provider.FileSystemFactoryProvider;
import com.webcodepro.applecommander.storage.DirectoryEntry;
import com.webcodepro.applecommander.storage.DiskException;
import com.webcodepro.applecommander.storage.DiskFullException;
import com.webcodepro.applecommander.storage.FileEntry;
import com.webcodepro.applecommander.storage.FileFilter;
import com.webcodepro.applecommander.storage.FormattedDisk;
import com.webcodepro.applecommander.storage.os.prodos.ProdosDirectoryEntry;
import com.webcodepro.applecommander.storage.os.prodos.ProdosFileEntry;
import vavi.nio.file.ac.AcFileSystemDriver.AcEntry;

import static java.util.function.Predicate.not;
import static vavi.nio.file.Util.isAppleDouble;


/**
 * AcFileSystemDriver.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2025/11/16 umjammer initial version <br>
 */
@ParametersAreNonnullByDefault
public final class AcFileSystemDriver extends ExtendedFileSystemDriver<AcEntry> {

    private static final Logger logger = System.getLogger(AcFileSystemDriver.class.getName());

    private FormattedDisk disk;

    /**
     * @param disk
     * @param env  { "ignoreAppleDouble": boolean }
     */
    public AcFileSystemDriver(FileStore fileStore,
                              FileSystemFactoryProvider provider,
                              FormattedDisk disk, Map<String, ?> env) throws IOException {

        super(fileStore, provider);

        this.disk = disk;
        setEnv(env);
    }

    private static final String ALTERNATIVE_SLASH = "\u2044";

    @Override
    protected String getFilenameString(AcEntry entry) {
        // there are cases in which Apple DOS filename contains '/'
        return entry.getFilename().replace(File.separator, ALTERNATIVE_SLASH);
    }

    private static String toLocalString(Path path) {
        return path.getFileName().toString().replace(ALTERNATIVE_SLASH, File.separator);
    }

    @Override
    protected boolean isFolder(AcEntry entry) throws IOException {
        return entry.isDirectory();
    }

    @Override
    protected boolean exists(AcEntry entry) throws IOException {
        return !entry.isDeleted();
    }

    // VFS might have cache?
    @Override
    protected AcEntry getEntry(Path path) throws IOException {
        if (ignoreAppleDouble && path.getFileName() != null && isAppleDouble(path)) {
            throw new NoSuchFileException("ignore apple double file: " + path);
        }

        try {
            AcEntry entry;
            if (path.getNameCount() == 0) {
//logger.log(Level.TRACE, "root");
                return new AcEntry(disk);
            } else {
                // TODO getFile support nested directories?
                String appleFilename = toLocalString(path);
                FileEntry fileEntry = disk.getFile(appleFilename);
                if (fileEntry == null) throw new NoSuchFileException(appleFilename);
                entry = new AcEntry(fileEntry);
            }
            return entry;
        } catch (DiskException e) {
            throw (NoSuchFileException) new NoSuchFileException(path.toString()).initCause(e);
        }
    }

    @Override
    protected InputStream downloadEntry(AcEntry entry, Path path, Set<? extends OpenOption> options) throws IOException {
        return new ByteArrayInputStream(entry.getFileData());
    }

    @Override
    protected OutputStream uploadEntry(AcEntry parentEntry, Path path, Set<? extends OpenOption> options) throws IOException {
        try {
            AcEntry fileEntry = new AcEntry(disk.createFile());
            return new ByteArrayOutputStream() {
                boolean done;
                @Override public void flush() {
                    try {
                        disk.setFileData(fileEntry, toByteArray());
                        done = true;
                    } catch (DiskException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override public void close() {
                    if (!done) flush();
                }
            };
        } catch (DiskException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<AcEntry> getDirectoryEntries(AcEntry dirEntry, Path dir) throws IOException {
        try {
            return dirEntry.getFiles().stream().filter(not(FileEntry::isDeleted)).map(AcEntry::new).toList();
        } catch (NullPointerException | DiskException e) {
logger.log(Level.TRACE, "dir: " + dirEntry.getFilename() + ", " + dirEntry.isDirectory());
            throw new IOException(e);
        }
    }

    @Override
    protected AcEntry createDirectoryEntry(AcEntry parentEntry, Path dir) throws IOException {
        try {
            if (disk.canCreateDirectories()) {
                throw new UnsupportedOperationException("doesn't support directory creation.");
            } else {
                return new AcEntry(parentEntry.createDirectory(dir.getFileName().toString()));
            }
        } catch (DiskException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected boolean hasChildren(AcEntry dirEntry, Path dir) throws IOException {
        try {
            return !dirEntry.getFiles().isEmpty();
        } catch (DiskException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void removeEntry(AcEntry entry, Path path) throws IOException {
        entry.delete();
    }

    @Override
    protected AcEntry copyEntry(AcEntry sourceEntry, AcEntry targetParentEntry, Path source, Path target, Set<CopyOption> options) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
//        FileEntry targetEntry = getEntry(target, false);
//        Files.copy(sourceEntry.toPath(), targetEntry.toPath());
//        return targetEntry;
    }

    @Override
    protected AcEntry moveEntry(AcEntry sourceEntry, AcEntry targetParentEntry, Path source, Path target, boolean targetIsParent) throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
//        FileEntry targetEntry = getEntry(targetIsParent ? target.resolve(toFilenameString(source)) : target, false);
//        Files.move(sourceEntry.toPath(), targetEntry.toPath());
//        return targetEntry;
    }

    @Override
    protected AcEntry moveFolderEntry(AcEntry sourceEntry, AcEntry targetParentEntry, Path source, Path target, boolean targetIsParent) throws IOException {
        return moveEntry(sourceEntry, targetParentEntry, source, target, targetIsParent);
    }

    @Override
    protected AcEntry renameEntry(AcEntry sourceEntry, AcEntry targetParentEntry, Path source, Path target) throws IOException {
        return moveEntry(sourceEntry, targetParentEntry, source, target, false);
    }

    /**
     * i don't like the specs. that separated file and directory interface like ac project.
     * bacause we need to create
     */
    public static class AcEntry implements FileEntry, DirectoryEntry {

        FileEntry fileEntry;
        DirectoryEntry directoryEntry;

        AcEntry(FileEntry fileEntry) {
            this.fileEntry = fileEntry;
        }

        AcEntry(DirectoryEntry directoryEntry) {
            this.directoryEntry = directoryEntry;
        }

        @Override
        public String getDirname() {
            return directoryEntry.getDirname();
        }

        @Override
        public List<FileEntry> getFiles() throws DiskException {
            if (fileEntry instanceof DirectoryEntry) return ((DirectoryEntry) fileEntry).getFiles();
            else if (directoryEntry != null) return directoryEntry.getFiles();
            else throw new IllegalStateException();
        }

        @Override
        public FileEntry createFile() throws DiskException {
            return directoryEntry.createFile();
        }

        @Override
        public DirectoryEntry createDirectory(String name) throws DiskException {
            return directoryEntry.createDirectory(name);
        }

        @Override
        public boolean canCreateDirectories() {
            return directoryEntry.canCreateDirectories();
        }

        @Override
        public boolean canCreateFile() {
            return directoryEntry.canCreateFile();
        }

        @Override
        public String getFilename() {
            return fileEntry.getFilename();
        }

        @Override
        public void setFilename(String filename) {
            fileEntry.setFilename(filename);
        }

        @Override
        public String getFiletype() {
            return fileEntry.getFiletype();
        }

        @Override
        public void setFiletype(String filetype) {
            fileEntry.setFiletype(filetype);
        }

        @Override
        public boolean isLocked() {
            return fileEntry.isLocked();
        }

        @Override
        public void setLocked(boolean lock) {
            fileEntry.setLocked(lock);
        }

        @Override
        public int getSize() {
            if (fileEntry != null) return fileEntry.getSize();
            else if (directoryEntry != null) return 0;
            else throw new IllegalStateException();
        }

        @Override
        public boolean isDirectory() {
            if (fileEntry != null) return fileEntry.isDirectory();
            else if (directoryEntry != null) return true;
            else throw new IllegalStateException();
        }

        @Override
        public boolean isDeleted() {
            return fileEntry.isDeleted();
        }

        @Override
        public void delete() {
            fileEntry.delete();
        }

        @Override
        public List<String> getFileColumnData(int displayMode) {
            return fileEntry.getFileColumnData(displayMode);
        }

        @Override
        public byte[] getFileData() {
            return fileEntry.getFileData();
        }

        @Override
        public void setFileData(byte[] data) throws DiskFullException {
            fileEntry.setFileData(data);
        }

        @Override
        public FileFilter getSuggestedFilter() {
            return fileEntry.getSuggestedFilter();
        }

        @Override
        public FormattedDisk getFormattedDisk() {
            if (fileEntry != null) return fileEntry.getFormattedDisk();
            else if (directoryEntry != null) return directoryEntry.getFormattedDisk();
            else throw new IllegalStateException();
        }

        @Override
        public int getMaximumFilenameLength() {
            return fileEntry.getMaximumFilenameLength();
        }

        @Override
        public boolean needsAddress() {
            return fileEntry.needsAddress();
        }

        @Override
        public void setAddress(int address) {
            fileEntry.setAddress(address);
        }

        @Override
        public int getAddress() {
            return fileEntry.getAddress();
        }

        public long getLastModificationDateTime() {
            if (fileEntry instanceof ProdosFileEntry prodosFileEntry) {
                return prodosFileEntry.getLastModificationDate() != null ? prodosFileEntry.getLastModificationDate().getTime() : 0;
            } else if (directoryEntry instanceof ProdosDirectoryEntry prodosDirectoryEntry) {
                return prodosDirectoryEntry.getLastModificationDate() != null ? prodosDirectoryEntry.getLastModificationDate().getTime() : 0;
            } else {
                return 0;
            }
        }

        public Object getWrappedObject() {
            if (fileEntry != null) return fileEntry;
            else if (directoryEntry != null) return directoryEntry;
            else throw new IllegalStateException();
        }
    }
}
