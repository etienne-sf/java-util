package com.cedarsoftware.util;

import java.util.*;

/**
 * `CompactMap` introduced.  This `Map` is especially small when 0 and 1 entries are stored in it.
 *
 *     When `>=2` entries are in the `Map` it acts as regular `Map`.
 *     You must override two methods in order to instantiate:
 *     
 *     protected abstract K getSingleValueKey();
 *     protected abstract Map<K, V> getNewMap();
 *
 *      **Empty**
 *      This class only has one (1) member variable of type `Object`.  If there are no entries in it, then the value of that
 *      member variable takes on a pointer (points to sentinel value.)
 *
 *      **One entry**
 *      If the entry has a key that matches the value returned from `getSingleValueKey()` then there is no key stored
 *      and the internal single member points to the value (still retried with 100% proper Map semantics).
 *
 *      If the single entry's key does not match the value returned from `getSingleValueKey()` then the internal field points
 *      to an internal `Class` `CompactMapEntry` which contains the key and the value (nothing else).  Again, all APIs still operate
 *      the same.
 *
 *      **Two or more entries**
 *      In this case, the single member variable points to a `Map` instance (supplied by `getNewMap()` API that user supplied.)
 *      This allows `CompactMap` to work with nearly all `Map` types.
 *
 *      A future version *may* support an additional option to allow it to maintain entries 2-n in an internal
 *      array (pointed to by the single member variable).  This small array would be 'scanned' in linear time.  Given
 *      a small *`n`*  entries, the resultant `Map` would be significantly smaller than the equivalent `HashMap`, for instance.
 *
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br>
 *         Copyright (c) Cedar Software LLC
 *         <br><br>
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br><br>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br><br>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
public abstract class CompactMap<K, V> implements Map<K, V>
{
    private static final String EMPTY_MAP = "_︿_ψ_☼";
    private Object val = EMPTY_MAP;

    public int size()
    {
        if (val == EMPTY_MAP)
        {
            return 0;
        }
        else if (isCompactMapEntry(val) || !(val instanceof Map))
        {
            return 1;
        }
        else
        {
            Map<K, V> map = (Map<K, V>) val;
            return map.size();
        }
    }

    public boolean isEmpty()
    {
        return val == EMPTY_MAP;
    }

    public boolean containsKey(Object key)
    {
        if (size() == 1)
        {
            return getLogicalSingleKey().equals(key);
        }
        else if (isEmpty())
        {
            return false;
        }

        Map<K, V> map = (Map<K, V>) val;
        return map.containsKey(key);
    }

    public boolean containsValue(Object value)
    {
        if (size() == 1)
        {
            return getLogicalSingleValue() == value;
        }
        else if (isEmpty())
        {
            return false;
        }
        Map<K, V> map = (Map<K, V>) val;
        return map.containsValue(value);
    }

    public V get(Object key)
    {
        if (size() == 1)
        {
            return getLogicalSingleKey().equals(key) ? getLogicalSingleValue() : null;
        }
        else if (isEmpty())
        {
            return null;
        }
        Map<K, V> map = (Map<K, V>) val;
        return map.get(key);
    }

    public V put(K key, V value)
    {
        if (size() == 1)
        {
            if (getLogicalSingleKey().equals(key))
            {   // Overwrite
                Object save = getLogicalSingleValue();
                if (getSingleValueKey().equals(key) && !(value instanceof Map))
                {
                    val = value;
                }
                else
                {
                    val = new CompactMapEntry(key, value);
                }
                return (V) save;
            }
            else
            {   // Add
                Map<K, V> map = getNewMap();
                map.put(getLogicalSingleKey(), getLogicalSingleValue());
                map.put(key, value);
                val = map;
                return null;
            }
        }
        else if (isEmpty())
        {
            if (getSingleValueKey().equals(key) && !(value instanceof Map))
            {
                val = value;
            }
            else
            {
                val = new CompactMapEntry(key, value);
            }
            return null;
        }
        Map<K, V> map = (Map<K, V>) val;
        return map.put(key, value);
    }

    public V remove(Object key)
    {
        if (size() == 1)
        {
            if (getLogicalSingleKey().equals(key))
            {   // found
                Object save = getLogicalSingleValue();
                val = EMPTY_MAP;
                return (V) save;
            }
            else
            {   // not found
                return null;
            }
        }
        else if (isEmpty())
        {
            return null;
        }

        // Handle from 2+ entries.
        Map<K, V> map = (Map<K, V>) val;
        V save = map.remove(key);
        
        if (map.size() == 1)
        {   // Down to 1 entry, need to set 'val' to value or CompactMapEntry containing key/value
            Entry<K, V> entry = map.entrySet().iterator().next();
            clear();
            put(entry.getKey(), entry.getValue());  // .put() will figure out how to place this entry
        }
        return save;
    }

    public void putAll(Map<? extends K, ? extends V> m)
    {
        for (Entry<? extends K, ? extends V> entry : m.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear()
    {
        val = EMPTY_MAP;
    }

    public Set<K> keySet()
    {
        return new AbstractSet<K>()
        {
            Iterator<K> iter;

            public Iterator<K> iterator()
            {
                if (CompactMap.this.size() == 1)
                {
                    Map<K, V> map = new HashMap<>();
                    map.put(getLogicalSingleKey(), (V)getLogicalSingleValue());
                    iter = map.keySet().iterator();
                    return new Iterator<K>()
                    {
                        public boolean hasNext() { return iter.hasNext(); }
                        public K next() { return iter.next(); }
                        public void remove() { CompactMap.this.clear(); }
                    };
                }
                else if (CompactMap.this.isEmpty())
                {
                    return new Iterator<K>()
                    {
                        public boolean hasNext() { return false; }
                        public K next() { throw new NoSuchElementException(".next() called on an empty CompactMap's keySet()"); }
                        public void remove() { throw new IllegalStateException(".remove() called on an empty CompactMap's keySet()"); }
                    };
                }

                // 2 or more elements in the CompactMap case.
                Map<K, V> map = (Map<K, V>)CompactMap.this.val;
                iter = map.keySet().iterator();

                return new Iterator<K>()
                {
                    public boolean hasNext() { return iter.hasNext(); }
                    public K next() { return iter.next(); }
                    public void remove() { removeIteratorItem(iter, "keySet"); }
                };
            }

            public int size() { return CompactMap.this.size(); }
            public boolean contains(Object o) { return CompactMap.this.containsKey(o); }
            public void clear() { CompactMap.this.clear(); }
        };
    }

    public Collection<V> values()
    {
        return new AbstractCollection<V>()
        {
            Iterator<V> iter;
            public Iterator<V> iterator()
            {
                if (CompactMap.this.size() == 1)
                {
                    Map<K, V> map = new HashMap<>();
                    map.put(getLogicalSingleKey(), (V)getLogicalSingleValue());
                    iter = map.values().iterator();
                    return new Iterator<V>()
                    {
                        public boolean hasNext() { return iter.hasNext(); }
                        public V next() { return iter.next(); }
                        public void remove() { CompactMap.this.clear(); }
                    };
                }
                else if (CompactMap.this.isEmpty())
                {
                    return new Iterator<V>()
                    {
                        public boolean hasNext() { return false; }
                        public V next() { throw new NoSuchElementException(".next() called on an empty CompactMap's values()"); }
                        public void remove() { throw new IllegalStateException(".remove() called on an empty CompactMap's values()"); }
                    };
                }

                // 2 or more elements in the CompactMap case.
                Map<K, V> map = (Map<K, V>)CompactMap.this.val;
                iter = map.values().iterator();

                return new Iterator<V>()
                {
                    public boolean hasNext() { return iter.hasNext(); }
                    public V next() { return iter.next(); }
                    public void remove() { removeIteratorItem(iter, "values"); }
                };
            }

            public int size() { return CompactMap.this.size(); }
            public void clear() { CompactMap.this.clear(); }
        };
    }

    public Set<Entry<K, V>> entrySet()
    {
        return new AbstractSet<Entry<K,V>>()
        {
            Iterator<Entry<K, V>> iter;

            public int size() { return CompactMap.this.size(); }

            public Iterator<Entry<K, V>> iterator()
            {
                if (CompactMap.this.size() == 1)
                {
                    Map<K, V> map = new HashMap<>();
                    map.put(getLogicalSingleKey(), getLogicalSingleValue());
                    iter = map.entrySet().iterator();
                    return new Iterator<Entry<K, V>>()
                    {
                        public boolean hasNext() { return iter.hasNext(); }
                        public Entry<K, V> next()
                        {
                            Entry<K,V> entry = iter.next();
                            return new CompactMapEntry(entry.getKey(), entry.getValue());
                        }
                        public void remove() { CompactMap.this.clear(); }
                    };
                }
                else if (CompactMap.this.isEmpty())
                {
                    return new Iterator<Entry<K, V>>()
                    {
                        public boolean hasNext() { return false; }
                        public Entry<K, V> next() { throw new NoSuchElementException(".next() called on an empty CompactMap's entrySet()"); }
                        public void remove() { throw new IllegalStateException(".remove() called on an empty CompactMap's entrySet()"); }
                    };
                }
                // 2 or more elements in the CompactMap case.
                Map<K, V> map = (Map<K, V>)CompactMap.this.val;
                iter = map.entrySet().iterator();

                return new Iterator<Entry<K, V>>()
                {
                    public boolean hasNext() { return iter.hasNext(); }
                    public Entry<K, V> next() { return iter.next(); }
                    public void remove() { removeIteratorItem(iter, "entrySet"); }
                };
            }
            public void clear() { CompactMap.this.clear(); }
        };
    }

    private void removeIteratorItem(Iterator iter, String methodName)
    {
        if (size() == 1)
        {
            clear();
        }
        else if (isEmpty())
        {
            throw new IllegalStateException(".remove() called on an empty CompactMap's " + methodName + " iterator");
        }
        else
        {
            if (size() == 2)
            {
                Iterator<Entry<K, V>> entryIterator = ((Map<K, V>) val).entrySet().iterator();
                Entry<K, V> firstEntry = entryIterator.next();
                Entry<K, V> secondEntry = entryIterator.next();
                clear();

                if (iter.hasNext())
                {   // .remove() called on 2nd element in 2 element list
                    put(secondEntry.getKey(), secondEntry.getValue());
                }
                else
                {   // .remove() called on 1st element in 1 element list
                    put(firstEntry.getKey(), firstEntry.getValue());
                }
            }
            else
            {
                iter.remove();
            }
        }
    }

    public Map minus(Object removeMe)
    {
        throw new UnsupportedOperationException("Unsupported operation [minus] or [-] between Maps.  Use removeAll() or retainAll() instead.");
    }

    public Map plus(Object right)
    {
        throw new UnsupportedOperationException("Unsupported operation [plus] or [+] between Maps.  Use putAll() instead.");
    }

    protected enum LogicalValueType
    {
        EMPTY, OBJECT, ENTRY, MAP
    }

    protected LogicalValueType getLogicalValueType()
    {
        if (size() == 1)
        {
            if (isCompactMapEntry(val))
            {
                return LogicalValueType.ENTRY;
            }
            else
            {
                return LogicalValueType.OBJECT;
            }
        }
        else if (isEmpty())
        {
            return LogicalValueType.EMPTY;
        }
        else
        {
            return LogicalValueType.MAP;
        }
    }

    /**
     * Marker Class to hold key and value when the key is not the same as the getSingleValueKey().
     * This method transmits the setValue() to changes on the outer CompactMap instance.
     */
    private class CompactMapEntry implements Entry<K, V>
    {
        K key;
        V value;

        private CompactMapEntry(K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        public K getKey() { return key; }
        public V getValue() { return value; }
        public V setValue(V value)
        {
            V save = this.value;
            this.value = value;
            CompactMap.this.put(key, value);    // "Transmit" write through to underlying Map.
            return save;
        }
    }
    
    private K getLogicalSingleKey()
    {
        if (isCompactMapEntry(val))
        {
            CompactMapEntry entry = (CompactMapEntry) val;
            return entry.getKey();
        }
        return getSingleValueKey();
    }

    private V getLogicalSingleValue()
    {
        if (isCompactMapEntry(val))
        {
            CompactMapEntry entry = (CompactMapEntry) val;
            return entry.getValue();
        }
        return (V) val;
    }

    private boolean isCompactMapEntry(Object o)
    {
        if (o == null) { return false; }
        return CompactMapEntry.class.isAssignableFrom(o.getClass());
    }
    
    /**
     * @return String key name when there is only one entry in the Map.
     */
    protected abstract K getSingleValueKey();

    /**
     * @return new empty Map instance to use when there is more than one entry.
     */
    protected abstract Map<K, V> getNewMap();
}