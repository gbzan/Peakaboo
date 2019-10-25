package org.peakaboo.framework.scratch.encoders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.peakaboo.framework.scratch.ScratchEncoder;
import org.peakaboo.framework.scratch.ScratchException;

public class CompoundEncoder<T> implements ScratchEncoder<T> {

	ScratchEncoder<T> first;
	List<ScratchEncoder<byte[]>> encoders;
	
	public CompoundEncoder(ScratchEncoder<T> first) {
		this.first = first;
		this.encoders = Collections.emptyList();
	}
	
	public CompoundEncoder(ScratchEncoder<T> first, List<ScratchEncoder<byte[]>> encoders) {
		this.first = first;
		this.encoders = encoders;
	}
	
	public CompoundEncoder(ScratchEncoder<T> first, ScratchEncoder<byte[]>... encoders) {
		this.first = first;
		this.encoders = Arrays.asList(encoders);
	}

	@Override
	public byte[] encode(T data) throws ScratchException {
		byte[] work = first.encode(data);
		for (int i = 0; i < encoders.size(); i++) {
			work = encoders.get(i).encode(work);
		}
		return work;
	}

	@Override
	public T decode(byte[] data) throws ScratchException {
		for (int i = 0; i < encoders.size(); i++) {
			data = encoders.get(i).decode(data);
		}
		return first.decode(data);
	}
	
	public String toString() {
		return encoders.stream().map(e -> e.toString()).reduce(first.toString(), (s1, s2) -> s1 + " -> " + s2);
	}
	
	
}
