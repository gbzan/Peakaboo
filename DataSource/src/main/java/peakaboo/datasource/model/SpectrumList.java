package peakaboo.datasource.model;

import java.io.IOException;
import java.util.List;

import scitypes.ISpectrum;
import scitypes.Spectrum;
import scratch.IScratchList;
import scratch.ScratchDiskList;
import scratch.ScratchList;
import scratch.encoders.CompoundEncoder;
import scratch.encoders.compressors.Compressors;
import scratch.encoders.compressors.LZ4CompressionEncoder;
import scratch.encoders.serializers.KryoSerializingEncoder;
import scratch.encoders.serializers.Serializers;

/**
 * SpectrumList is an implementation of the ListTests interface which writes 
 * out values to a temporary file, rather than storing elements in memory.
 * This is useful for lists which are sufficiently large to cause memory
 * concerns in a typical JVM.
 * <br /><br /> 
 * Storing elements on disk means that get operations will return copies 
 * of the objects in the list rather than the originally stored objects. 
 * If an element is retrieved from the list, modified, and then 
 * retrieved a second time, the second copy retrieved will lack the 
 * modifications made to the first copy.
 * 
 * To create a new SpectrumList, call {@link SpectrumList#create(String)}.
 * If the SpectrumList cannot be created for whatever reason, a memory-based
 * list will be created instead.
 * <br/><br/>
 * Note that this class depends on the specific implementation of Spectrum
 * being {@link ISpectrum} 
 * @author Nathaniel Sherry, 2011-2012
 *
 */

public final class SpectrumList {

	
	@SuppressWarnings("unchecked")
	public static List<Spectrum> create(String name)
	{
		IScratchList<Spectrum> backing;
		try {
			backing = new ScratchDiskList<Spectrum>();
		} catch (IOException e) {
			e.printStackTrace();
			backing = new ScratchList<Spectrum>();
		}
		backing.setEncoder(new CompoundEncoder<>(Serializers.kryo(ISpectrum.class), Compressors.lz4()));
		return backing;
	}
	
}
