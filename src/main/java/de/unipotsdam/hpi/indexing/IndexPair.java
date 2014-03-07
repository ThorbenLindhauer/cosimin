/*
 * Copyright 2014 Sebastian Kruse, Thorben Lindhauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
