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
