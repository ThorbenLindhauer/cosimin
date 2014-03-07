package de.unipotsdam.hpi.indexing;

import java.util.Arrays;
import java.util.Comparator;

import de.unipotsdam.hpi.util.BitSignatureUtil;

public class IndexPair {

	private long[] bitSignature;
	private int elementId;

	public IndexPair() {

	}

	public IndexPair(long[] bitSignature, int elementId) {
		this.bitSignature = bitSignature;
		this.elementId = elementId;
	}

	public long[] getBitSignature() {
		return bitSignature;
	}

	public int getElementId() {
		return elementId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof IndexPair) {
			IndexPair otherIndexPair = (IndexPair) obj;
			return this.elementId == otherIndexPair.elementId
					&& Arrays.equals(this.bitSignature,
							otherIndexPair.bitSignature);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ~elementId + Arrays.hashCode(bitSignature);
	}
	
	@Override
	public String toString() {
		return String.format("[%d, %s]", elementId, Arrays.toString(bitSignature));
	}
	
	public static final Comparator<IndexPair> COMPARATOR = new Comparator<IndexPair>() {

		public int compare(IndexPair ip1, IndexPair ip2) {
			return BitSignatureUtil.COMPARATOR.compare(ip1.bitSignature, ip2.bitSignature);
		}
	};

}
