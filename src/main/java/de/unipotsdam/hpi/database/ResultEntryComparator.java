package de.unipotsdam.hpi.database;

import java.util.Comparator;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry;

public class ResultEntryComparator implements Comparator<Entry> {

  public int compare(Entry entry1, Entry entry2) {
    if (entry1.getDoubleValue() < entry2.getDoubleValue()) {
      return -1;
    } else if (entry1.getDoubleValue() > entry2.getDoubleValue()) {
      return 1;
    } else {
      if (entry1.getIntKey() < entry2.getIntKey()) {
        return -1;
      } else if (entry1.getIntKey() > entry2.getIntKey()) {
        return 1;
      } else {
        return 0;
      }
    }
  }
}
