package org.peakaboo.datasource.model.datafile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import org.peakaboo.datasource.model.DataSource;

/**
 * A DataFile is a representation of data that does not necessarily exist on
 * disk in an accessible location. The data can either be accessed through an
 * {@link InputStream}, or through a {@link Path}. The InputStream should be
 * preferred, as the file referenced by the {@link Path} may be created on
 * demand as a temporary file copied from the InputStream.
 * 
 * @author NAS
 *
 */
public interface DataFile extends AutoCloseable {

	/**
	 * Gets a relative filename for this DataFile.
	 */
	String getFilename();

	/**
	 * Returns an {@link InputStream} for this DataFile
	 * 
	 * @throws IOException
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Ensures that the data is available as a file on disk (or some filesystem
	 * accessible by the java.nio system), and returns a {@link Path} to that
	 * file. <br/>
	 * <br/>
	 * Note that for some input sources, the data may not originate from a file on
	 * disk, and this step may incur extra overhead compared to
	 * {@link #getInputStream()}. This is useful for {@link DataSource}s which wrap
	 * native libraries, and which cannot make use of Java constructs like
	 * {@link InputStream}s. To minimize overhead and resource consumotion,
	 * DataSources are encouraged to call {@link #close()} on a DataFile after it
	 * has been read to clean up any temporary files created in the process.
	 * 
	 * @throws IOException
	 */
	Path getAndEnsurePath() throws IOException;

	/**
	 * Returns the size of the file or stream if available
	 */
	Optional<Long> size();
	
	
	/**
	 * Indicates if this resource can be represented as a String and re-accessed
	 * later. Some formats may rely on having a "magic" object passed to the
	 * constructor which cannot be serialized and re-accessed.
	 * 
	 * @return true if is it addressable (re-accessable), false otherwise
	 */
	default boolean addressable() {
		return address().isPresent();
	}
	
	/**
	 * Provide the address of this resource, if it is addressable
	 * @return an Optional<String> representation of the address if it is addressable, empty otherwise
	 */
	Optional<String> address();
	

	/**
	 * Tests if the resource is currently accessable.
	 * @return true if the resource is accessible, false otherwise
	 */
	boolean exists();

	/**
	 * Reports if the resource can be written to as well as read 
	 * @return true if the source is writable, false otherwise
	 */
	boolean writable();
	
	/**
	 * If applicable, returns the local folder which contains the datafile
	 * @return a Optional<File> object representing the file's parent directory if applicable, an empty Optional otherwise
	 */
	Optional<File> localFolder();
	
	
}
