package com.cedarsoftware.util;

import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.*;

/**
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
public class TestCaseInsensitiveMap
{
    @Test
    public void testMapStraightUp()
    {
        CaseInsensitiveMap<String, Object> stringMap = createSimpleMap();

        assertTrue(stringMap.get("one").equals("Two"));
        assertTrue(stringMap.get("One").equals("Two"));
        assertTrue(stringMap.get("oNe").equals("Two"));
        assertTrue(stringMap.get("onE").equals("Two"));
        assertTrue(stringMap.get("ONe").equals("Two"));
        assertTrue(stringMap.get("oNE").equals("Two"));
        assertTrue(stringMap.get("ONE").equals("Two"));

        assertFalse(stringMap.get("one").equals("two"));

        assertTrue(stringMap.get("three").equals("Four"));
        assertTrue(stringMap.get("fIvE").equals("Six"));
    }

    @Test
    public void testWithNonStringKeys()
    {
        CaseInsensitiveMap stringMap = new CaseInsensitiveMap();
        assert stringMap.isEmpty();

        stringMap.put(97, "eight");
        stringMap.put(19, "nineteen");
        stringMap.put("a", "two");
        stringMap.put("three", "four");
        stringMap.put(null, "null");

        assertEquals("two", stringMap.get("a"));
        assertEquals("four", stringMap.get("three"));
        assertNull(stringMap.get(8L));
        assertEquals("nineteen", stringMap.get(19));
        assertEquals("null", stringMap.get(null));
    }

    @Test
    public void testOverwrite()
    {
        CaseInsensitiveMap<String, Object> stringMap = createSimpleMap();

        assertTrue(stringMap.get("three").equals("Four"));

        stringMap.put("thRee", "Thirty");

        assertFalse(stringMap.get("three").equals("Four"));
        assertTrue(stringMap.get("three").equals("Thirty"));
        assertTrue(stringMap.get("THREE").equals("Thirty"));
    }

    @Test
    public void testKeySetWithOverwriteAttempt()
    {
        CaseInsensitiveMap<String, Object> stringMap = createSimpleMap();

        stringMap.put("thREe", "Four");

        Set<String> keySet = stringMap.keySet();
        assertNotNull(keySet);
        assertTrue(!keySet.isEmpty());
        assertTrue(keySet.size() == 3);

        boolean foundOne = false, foundThree = false, foundFive = false;
        for (String key : keySet)
        {
            if (key.equals("One"))
            {
                foundOne = true;
            }
            if (key.equals("Three"))
            {
                foundThree = true;
            }
            if (key.equals("Five"))
            {
                foundFive = true;
            }
        }
        assertTrue(foundOne);
        assertTrue(foundThree);
        assertTrue(foundFive);
    }

    @Test
    public void testEntrySetWithOverwriteAttempt()
    {
        CaseInsensitiveMap<String, Object> stringMap = createSimpleMap();

        stringMap.put("thREe", "four");

        Set<Map.Entry<String, Object>> entrySet = stringMap.entrySet();
        assertNotNull(entrySet);
        assertTrue(entrySet.size() == 3);

        boolean foundOne = false, foundThree = false, foundFive = false;
        for (Map.Entry<String, Object> entry : entrySet)
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals("One") && value.equals("Two"))
            {
                foundOne = true;
            }
            if (key.equals("Three") && value.equals("four"))
            {
                foundThree = true;
            }
            if (key.equals("Five") && value.equals("Six"))
            {
                foundFive = true;
            }
        }
        assertTrue(foundOne);
        assertTrue(foundThree);
        assertTrue(foundFive);
    }

    @Test
    public void testPutAll()
    {
        CaseInsensitiveMap<String, Object> stringMap = createSimpleMap();
        CaseInsensitiveMap<String, Object> newMap = new CaseInsensitiveMap<String, Object>(2);
        newMap.put("thREe", "four");
        newMap.put("Seven", "Eight");

        stringMap.putAll(newMap);

        assertTrue(stringMap.size() == 4);
        assertFalse(stringMap.get("one").equals("two"));
        assertTrue(stringMap.get("fIvE").equals("Six"));
        assertTrue(stringMap.get("three").equals("four"));
        assertTrue(stringMap.get("seven").equals("Eight"));

        Map a = createSimpleMap();
        a.putAll(null);     // Ensure NPE not happening
    }

    @Test
    public void testContainsKey()
    {
        CaseInsensitiveMap<String, Object> stringMap = createSimpleMap();

        assertTrue(stringMap.containsKey("one"));
        assertTrue(stringMap.containsKey("One"));
        assertTrue(stringMap.containsKey("oNe"));
        assertTrue(stringMap.containsKey("onE"));
        assertTrue(stringMap.containsKey("ONe"));
        assertTrue(stringMap.containsKey("oNE"));
        assertTrue(stringMap.containsKey("ONE"));
    }

    @Test
    public void testRemove()
    {
        CaseInsensitiveMap<String, Object> stringMap = createSimpleMap();

        assertTrue(stringMap.remove("one").equals("Two"));
        assertNull(stringMap.get("one"));
    }

    @Test
    public void testNulls()
    {
        CaseInsensitiveMap<String, Object> stringMap = createSimpleMap();

        stringMap.put(null, "Something");
        assertTrue("Something".equals(stringMap.get(null)));
    }

    @Test
    public void testRemoveIterator()
    {
        Map map = new CaseInsensitiveMap();
        map.put("One", null);
        map.put("Two", null);
        map.put("Three", null);

        int count = 0;
        Iterator i = map.keySet().iterator();
        while (i.hasNext())
        {
            i.next();
            count++;
        }

        assertEquals(3, count);

        i = map.keySet().iterator();
        while (i.hasNext())
        {
            Object elem = i.next();
            if (elem.equals("One"))
            {
                i.remove();
            }
        }

        assertEquals(2, map.size());
        assertFalse(map.containsKey("one"));
        assertTrue(map.containsKey("two"));
        assertTrue(map.containsKey("three"));
    }

    @Test
    public void testEquals()
    {
        Map a = createSimpleMap();
        Map b = createSimpleMap();
        assertTrue(a.equals(b));
        Map c = new HashMap();
        assertFalse(a.equals(c));

        Map other = new LinkedHashMap();
        other.put("one", "Two");
        other.put("THREe", "Four");
        other.put("five", "Six");

        assertTrue(a.equals(other));
        assertTrue(other.equals(a));

        other.clear();
        other.put("one", "Two");
        other.put("Three-x", "Four");
        other.put("five", "Six");
        assertFalse(a.equals(other));

        other.clear();
        other.put("One", "Two");
        other.put("Three", "Four");
        other.put("Five", "six");   // lowercase six
        assertFalse(a.equals(other));

        assertFalse(a.equals("Foo"));

        other.put("FIVE", null);
        assertFalse(a.equals(other));

        a = createSimpleMap();
        b = createSimpleMap();
        a.put("Five", null);
        assertNotEquals(a, b);
    }

    @Test
    public void testEquals1()
    {
        Map<String, Object> map1 = new CaseInsensitiveMap<>();
        Map<String, Object> map2 = new CaseInsensitiveMap<>();
        assert map1.equals(map2);
        assert map1.equals(map1);
    }

    @Test
    public void testHashCode()
    {
        Map a = createSimpleMap();
        Map b = new CaseInsensitiveMap(a);
        assertTrue(a.hashCode() == b.hashCode());

        b = new CaseInsensitiveMap();
        b.put("ONE", "Two");
        b.put("THREE", "Four");
        b.put("FIVE", "Six");
        assertEquals(a.hashCode(), b.hashCode());

        b = new CaseInsensitiveMap();
        b.put("One", "Two");
        b.put("THREE", "FOUR");
        b.put("Five", "Six");
        assertFalse(a.hashCode() == b.hashCode());  // value FOUR is different than Four
    }

    @Test
    public void testHashcodeWithNullInKeys()
    {
        Map<String, String> map = new CaseInsensitiveMap<>();
        map.put("foo", "bar");
        map.put("baz", "qux");
        map.put(null, "quux");

        assert map.keySet().hashCode() != 0;
    }

    @Test
    public void testToString()
    {
        assertNotNull(createSimpleMap().toString());
    }

    @Test
    public void testClear()
    {
        Map a = createSimpleMap();
        a.clear();
        assertEquals(0, a.size());
    }

    @Test
    public void testContainsValue()
    {
        Map a = createSimpleMap();
        assertTrue(a.containsValue("Two"));
        assertFalse(a.containsValue("TWO"));
    }

    @Test
    public void testValues()
    {
        Map a = createSimpleMap();
        Collection col = a.values();
        assertEquals(3, col.size());
        assertTrue(col.contains("Two"));
        assertTrue(col.contains("Four"));
        assertTrue(col.contains("Six"));
        assertFalse(col.contains("TWO"));

        a.remove("one");
        assert col.size() == 2;
    }

    @Test
    public void testNullKey()
    {
        Map a = createSimpleMap();
        a.put(null, "foo");
        String b = (String) a.get(null);
        int x = b.hashCode();
        assertEquals("foo", b);
    }

    @Test
    public void testConstructors()
    {
        Map<String, Object> map = new CaseInsensitiveMap<String, Object>();
        map.put("BTC", "Bitcoin");
        map.put("LTC", "Litecoin");

        assertTrue(map.size() == 2);
        assertEquals("Bitcoin", map.get("btc"));
        assertEquals("Litecoin", map.get("ltc"));

        map = new CaseInsensitiveMap<String, Object>(20);
        map.put("BTC", "Bitcoin");
        map.put("LTC", "Litecoin");

        assertTrue(map.size() == 2);
        assertEquals("Bitcoin", map.get("btc"));
        assertEquals("Litecoin", map.get("ltc"));

        map = new CaseInsensitiveMap<String, Object>(20, 0.85f);
        map.put("BTC", "Bitcoin");
        map.put("LTC", "Litecoin");

        assertTrue(map.size() == 2);
        assertEquals("Bitcoin", map.get("btc"));
        assertEquals("Litecoin", map.get("ltc"));

        Map map1 = new HashMap<String, Object>();
        map1.put("BTC", "Bitcoin");
        map1.put("LTC", "Litecoin");

        map = new CaseInsensitiveMap<String, Object>(map1);
        assertTrue(map.size() == 2);
        assertEquals("Bitcoin", map.get("btc"));
        assertEquals("Litecoin", map.get("ltc"));
    }

    @Test
    public void testEqualsAndHashCode()
    {
        Map map1 = new HashMap();
        map1.put("BTC", "Bitcoin");
        map1.put("LTC", "Litecoin");
        map1.put(16, 16);
        map1.put(null, null);

        Map map2 = new CaseInsensitiveMap();
        map2.put("BTC", "Bitcoin");
        map2.put("LTC", "Litecoin");
        map2.put(16, 16);
        map2.put(null, null);

        Map map3 = new CaseInsensitiveMap();
        map3.put("btc", "Bitcoin");
        map3.put("ltc", "Litecoin");
        map3.put(16, 16);
        map3.put(null, null);

        assertTrue(map1.hashCode() != map2.hashCode());    // By design: case sensitive maps will [rightly] compute hash of ABC and abc differently
        assertTrue(map1.hashCode() != map3.hashCode());    // By design: case sensitive maps will [rightly] compute hash of ABC and abc differently
        assertTrue(map2.hashCode() == map3.hashCode());

        assertTrue(map1.equals(map2));
        assertTrue(map1.equals(map3));
        assertTrue(map3.equals(map1));
        assertTrue(map2.equals(map3));
    }

    // --------- Test returned keySet() operations ---------

    @Test
    public void testKeySetContains()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();
        assertTrue(s.contains("oNe"));
        assertTrue(s.contains("thRee"));
        assertTrue(s.contains("fiVe"));
        assertFalse(s.contains("dog"));
    }

    @Test
    public void testKeySetContainsAll()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();
        Set items = new HashSet();
        items.add("one");
        items.add("five");
        assertTrue(s.containsAll(items));
        items.add("dog");
        assertFalse(s.containsAll(items));
    }

    @Test
    public void testKeySetRemove()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();

        s.remove("Dog");
        assertEquals(3, m.size());
        assertEquals(3, s.size());

        assertTrue(s.remove("oNe"));
        assertTrue(s.remove("thRee"));
        assertTrue(s.remove("fiVe"));
        assertEquals(0, m.size());
        assertEquals(0, s.size());
    }

    @Test
    public void testKeySetRemoveAll()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();
        Set items = new HashSet();
        items.add("one");
        items.add("five");
        assertTrue(s.removeAll(items));
        assertEquals(1, m.size());
        assertEquals(1, s.size());
        assertTrue(s.contains("three"));
        assertTrue(m.containsKey("three"));

        items.clear();
        items.add("dog");
        s.removeAll(items);
        assertEquals(1, m.size());
        assertEquals(1, s.size());
        assertTrue(s.contains("three"));
        assertTrue(m.containsKey("three"));
    }

    @Test
    public void testKeySetRetainAll()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();
        Set items = new HashSet();
        items.add("three");
        assertTrue(s.retainAll(items));
        assertEquals(1, m.size());
        assertEquals(1, s.size());
        assertTrue(s.contains("three"));
        assertTrue(m.containsKey("three"));

        m = createSimpleMap();
        s = m.keySet();
        items.clear();
        items.add("dog");
        items.add("one");
        assertTrue(s.retainAll(items));
        assertEquals(1, m.size());
        assertEquals(1, s.size());
        assertTrue(s.contains("one"));
        assertTrue(m.containsKey("one"));
    }

    @Test
    public void testKeySetToObjectArray()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();
        Object[] array = s.toArray();
        assertEquals(array[0], "One");
        assertEquals(array[1], "Three");
        assertEquals(array[2], "Five");
    }

    @Test
    public void testKeySetToTypedArray()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();
        String[] array = (String[]) s.toArray(new String[]{});
        assertEquals(array[0], "One");
        assertEquals(array[1], "Three");
        assertEquals(array[2], "Five");

        array = (String[]) s.toArray(new String[4]);
        assertEquals(array[0], "One");
        assertEquals(array[1], "Three");
        assertEquals(array[2], "Five");
        assertEquals(array[3], null);
        assertEquals(4, array.length);

        array = (String[]) s.toArray(new String[]{"","",""});
        assertEquals(array[0], "One");
        assertEquals(array[1], "Three");
        assertEquals(array[2], "Five");
        assertEquals(3, array.length);
    }

    @Test
    public void testKeySetToArrayDifferentKeyTypes()
    {
        Map<Object, Object> map = new CaseInsensitiveMap<>();
        map.put("foo", "bar");
        map.put(1.0d, 0.0d);
        map.put(true, false);
        map.put(Boolean.FALSE, Boolean.TRUE);
        Object[] keys = map.keySet().toArray();
        assert keys[0] == "foo";
        assert keys[1] instanceof Double;
        assert 1.0d == (double)keys[1];
        assert keys[2] instanceof Boolean;
        assert true == (boolean)keys[2];
        assert keys[3] instanceof Boolean;
        assert Boolean.FALSE == keys[3];
    }

    @Test
    public void testKeySetClear()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();
        s.clear();
        assertEquals(0, m.size());
        assertEquals(0, s.size());
    }

    @Test
    public void testKeySetHashCode()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();
        int h = s.hashCode();
        Set s2 = new HashSet();
        s2.add("One");
        s2.add("Three");
        s2.add("Five");
        assertNotEquals(h, s2.hashCode());

        s2 = new CaseInsensitiveSet();
        s2.add("One");
        s2.add("Three");
        s2.add("Five");
        assertEquals(h, s2.hashCode());
    }

    @Test
    public void testKeySetIteratorActions()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();
        Iterator i = s.iterator();
        Object o = i.next();
        assertTrue(o instanceof String);
        i.remove();
        assertEquals(2, m.size());
        assertEquals(2, s.size());

        o = i.next();
        assertTrue(o instanceof String);
        i.remove();
        assertEquals(1, m.size());
        assertEquals(1, s.size());

        o = i.next();
        assertTrue(o instanceof String);
        i.remove();
        assertEquals(0, m.size());
        assertEquals(0, s.size());
    }

    @Test
    public void testKeySetEquals()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();

        Set s2 = new HashSet();
        s2.add("One");
        s2.add("Three");
        s2.add("Five");
        assertTrue(s2.equals(s));
        assertTrue(s.equals(s2));

        Set s3 = new HashSet();
        s3.add("one");
        s3.add("three");
        s3.add("five");
        assertFalse(s3.equals(s));
        assertTrue(s.equals(s3));

        Set s4 = new CaseInsensitiveSet();
        s4.add("one");
        s4.add("three");
        s4.add("five");
        assertTrue(s4.equals(s));
        assertTrue(s.equals(s4));
    }

    @Test
    public void testKeySetAddNotSupported()
    {
        Map m = createSimpleMap();
        Set s = m.keySet();
        try
        {
            s.add("Bitcoin");
            fail("should not make it here");
        }
        catch (UnsupportedOperationException e)
        { }

        Set items = new HashSet();
        items.add("Food");
        items.add("Water");

        try
        {
            s.addAll(items);
            fail("should not make it here");
        }
        catch (UnsupportedOperationException e)
        { }
    }

    // ---------------- returned Entry Set tests ---------

    @Test
    public void testEntrySetContains()
    {
        Map m = createSimpleMap();
        Set s = m.entrySet();
        assertTrue(s.contains(getEntry("one", "Two")));
        assertTrue(s.contains(getEntry("tHree", "Four")));
        assertFalse(s.contains(getEntry("one", "two")));    // Value side is case-sensitive (needs 'Two' not 'two')

        assertFalse(s.contains("Not an entry"));
    }

    @Test
    public void testEntrySetContainsAll()
    {
        Map m = createSimpleMap();
        Set s = m.entrySet();
        Set items = new HashSet();
        items.add(getEntry("one", "Two"));
        items.add(getEntry("thRee", "Four"));
        assertTrue(s.containsAll(items));

        items = new HashSet();
        items.add(getEntry("one", "two"));
        items.add(getEntry("thRee", "Four"));
        assertFalse(s.containsAll(items));
    }

    @Test
    public void testEntrySetRemove()
    {
        Map m = createSimpleMap();
        Set s = m.entrySet();

        assertFalse(s.remove(getEntry("Cat", "Six")));
        assertEquals(3, m.size());
        assertEquals(3, s.size());

        assertTrue(s.remove(getEntry("oNe", "Two")));
        assertTrue(s.remove(getEntry("thRee", "Four")));

        assertFalse(s.remove(getEntry("Dog", "Two")));
        assertEquals(1, m.size());
        assertEquals(1, s.size());

        assertTrue(s.remove(getEntry("fiVe", "Six")));
        assertEquals(0, m.size());
        assertEquals(0, s.size());
    }

    @Test
    public void testEntrySetRemoveAll()
    {
        // Pure JDK test that fails
//        LinkedHashMap<String, Object> mm = new LinkedHashMap<String, Object>();
//        mm.put("One", "Two");
//        mm.put("Three", "Four");
//        mm.put("Five", "Six");
//        Set ss = mm.entrySet();
//        Set itemz = new HashSet();
//        itemz.add(getEntry("One", "Two"));
//        itemz.add(getEntry("Five", "Six"));
//        ss.removeAll(itemz);
//
//        itemz.clear();
//        itemz.add(getEntry("dog", "Two"));
//        assertFalse(ss.removeAll(itemz));
//        assertEquals(1, mm.size());
//        assertEquals(1, ss.size());
//        assertTrue(ss.contains(getEntry("Three", "Four")));
//        assertTrue(mm.containsKey("Three"));
//
//        itemz.clear();
//        itemz.add(getEntry("Three", "Four"));
//        assertTrue(ss.removeAll(itemz));  // fails - bug in JDK (Watching to see if this gets fixed)
//        assertEquals(0, mm.size());
//        assertEquals(0, ss.size());

        // Cedar Software code handles removeAll from entrySet perfectly
        Map m = createSimpleMap();
        Set s = m.entrySet();
        Set items = new HashSet();
        items.add(getEntry("one", "Two"));
        items.add(getEntry("five", "Six"));
        assertTrue(s.removeAll(items));
        assertEquals(1, m.size());
        assertEquals(1, s.size());
        assertTrue(s.contains(getEntry("three", "Four")));
        assertTrue(m.containsKey("three"));

        items.clear();
        items.add(getEntry("dog", "Two"));
        assertFalse(s.removeAll(items));
        assertEquals(1, m.size());
        assertEquals(1, s.size());
        assertTrue(s.contains(getEntry("three", "Four")));
        assertTrue(m.containsKey("three"));

        items.clear();
        items.add(getEntry("three", "Four"));
        assertTrue(s.removeAll(items));
        assertEquals(0, m.size());
        assertEquals(0, s.size());
    }

    @Test
    public void testEntrySetRetainAll()
    {
        Map m = createSimpleMap();
        Set s = m.entrySet();
        Set items = new HashSet();
        items.add(getEntry("three", "Four"));
        assertTrue(s.retainAll(items));
        assertEquals(1, m.size());
        assertEquals(1, s.size());
        assertTrue(s.contains(getEntry("three", "Four")));
        assertTrue(m.containsKey("three"));

        items.clear();
        items.add("dog");
        assertTrue(s.retainAll(items));
        assertEquals(0, m.size());
        assertEquals(0, s.size());
    }

    @Test
    public void testEntrySetRetainAll2()
    {
        Map m = createSimpleMap();
        Set s = m.entrySet();
        Set items = new HashSet();
        items.add(getEntry("three", null));
        assertTrue(s.retainAll(items));
        assertEquals(0, m.size());
        assertEquals(0, s.size());

        m = createSimpleMap();
        s = m.entrySet();
        items.clear();
        items.add(getEntry("three", 16));
        assertTrue(s.retainAll(items));
        assertEquals(0, m.size());
        assertEquals(0, s.size());
    }

    @Test
    public void testEntrySetRetainAll3()
    {
        Map<String, Object> map1 = new CaseInsensitiveMap<>();
        Map<String, Object> map2 = new CaseInsensitiveMap<>();

        map1.put("foo", "bar");
        map1.put("baz", "qux");
        map2.putAll(map1);

        assert !map1.entrySet().retainAll(map2.entrySet());
        assert map1.equals(map2);
    }

    @Test
    public void testEntrySetToObjectArray()
    {
        Map m = createSimpleMap();
        Set s = m.entrySet();
        Object[] array = s.toArray();
        assertEquals(3, array.length);

        Map.Entry entry = (Map.Entry) array[0];
        assertEquals("One", entry.getKey());
        assertEquals("Two", entry.getValue());

        entry = (Map.Entry) array[1];
        assertEquals("Three", entry.getKey());
        assertEquals("Four", entry.getValue());

        entry = (Map.Entry) array[2];
        assertEquals("Five", entry.getKey());
        assertEquals("Six", entry.getValue());
    }

    @Test
    public void testEntrySetToTypedArray()
    {
        Map m = createSimpleMap();
        Set s = m.entrySet();
        Map.Entry[] array = (Map.Entry[]) s.toArray(new Map.Entry[]{});
        assertEquals(array[0], getEntry("One", "Two"));
        assertEquals(array[1], getEntry("Three", "Four"));
        assertEquals(array[2], getEntry("Five", "Six"));

        s = m.entrySet();   // Should not need to do this (JDK has same issue)
        array = (Map.Entry[]) s.toArray(new Map.Entry[4]);
        assertEquals(array[0], getEntry("One", "Two"));
        assertEquals(array[1], getEntry("Three", "Four"));
        assertEquals(array[2], getEntry("Five", "Six"));
        assertEquals(array[3], null);
        assertEquals(4, array.length);

        s = m.entrySet();
        array = (Map.Entry[]) s.toArray(new Map.Entry[]{getEntry(1, 1), getEntry(2, 2), getEntry(3, 3)});
        assertEquals(array[0], getEntry("One", "Two"));
        assertEquals(array[1], getEntry("Three", "Four"));
        assertEquals(array[2], getEntry("Five", "Six"));
        assertEquals(3, array.length);
    }

    @Test
    public void testEntrySetClear()
    {
        Map m = createSimpleMap();
        Set s = m.entrySet();
        s.clear();
        assertEquals(0, m.size());
        assertEquals(0, s.size());
    }

    @Test
    public void testEntrySetHashCode()
    {
        Map m = createSimpleMap();
        Map m2 = new CaseInsensitiveMap();
        m2.put("one", "Two");
        m2.put("three", "Four");
        m2.put("five", "Six");
        assertEquals(m.hashCode(), m2.hashCode());

        Map m3 = new LinkedHashMap();
        m3.put("One", "Two");
        m3.put("Three", "Four");
        m3.put("Five", "Six");
        assertNotEquals(m.hashCode(), m3.hashCode());
    }

    @Test
    public void testEntrySetIteratorActions()
    {
        Map m = createSimpleMap();
        Set s = m.entrySet();
        Iterator i = s.iterator();
        Object o = i.next();
        assertTrue(o instanceof Map.Entry);
        i.remove();
        assertEquals(2, m.size());
        assertEquals(2, s.size());

        o = i.next();
        assertTrue(o instanceof Map.Entry);
        i.remove();
        assertEquals(1, m.size());
        assertEquals(1, s.size());

        o = i.next();
        assertTrue(o instanceof Map.Entry);
        i.remove();
        assertEquals(0, m.size());
        assertEquals(0, s.size());
    }

    @Test
    public void testEntrySetEquals()
    {
        Map m = createSimpleMap();
        Set s = m.entrySet();

        Set s2 = new HashSet();
        s2.add(getEntry("One", "Two"));
        s2.add(getEntry("Three", "Four"));
        s2.add(getEntry("Five", "Six"));
        assertTrue(s.equals(s2));

        s2.clear();
        s2.add(getEntry("One", "Two"));
        s2.add(getEntry("Three", "Four"));
        s2.add(getEntry("Five", "six"));    // lowercase six
        assertFalse(s.equals(s2));

        s2.clear();
        s2.add(getEntry("One", "Two"));
        s2.add(getEntry("Thre", "Four"));   // missing 'e' on three
        s2.add(getEntry("Five", "Six"));
        assertFalse(s.equals(s2));

        Set s3 = new HashSet();
        s3.add(getEntry("one", "Two"));
        s3.add(getEntry("three", "Four"));
        s3.add(getEntry("five","Six"));
        assertTrue(s.equals(s3));

        Set s4 = new CaseInsensitiveSet();
        s4.add(getEntry("one", "Two"));
        s4.add(getEntry("three", "Four"));
        s4.add(getEntry("five","Six"));
        assertTrue(s.equals(s4));

        CaseInsensitiveMap<String, Object> secondStringMap = createSimpleMap();
        assertFalse(s.equals("one"));

        assertTrue(s.equals(secondStringMap.entrySet()));
        // case-insensitive
        secondStringMap.put("five", "Six");
        assertTrue(s.equals(secondStringMap.entrySet()));
        secondStringMap.put("six", "sixty");
        assertFalse(s.equals(secondStringMap.entrySet()));
        secondStringMap.remove("five");
        assertFalse(s.equals(secondStringMap.entrySet()));
        secondStringMap.put("five", null);
        secondStringMap.remove("six");
        assertFalse(s.equals(secondStringMap.entrySet()));
        m.put("five", null);
        assertTrue(m.entrySet().equals(secondStringMap.entrySet()));
    }

    @Test
    public void testEntrySetAddNotSupport()
    {
        Map m = createSimpleMap();
        Set s = m.entrySet();

        try
        {
            s.add(10);
            fail("should not make it here");
        }
        catch (UnsupportedOperationException e)
        { }

        Set s2 = new HashSet();
        s2.add("food");
        s2.add("water");

        try
        {
            s.addAll(s2);
            fail("should not make it here");
        }
        catch (UnsupportedOperationException e)
        { }
    }

    @Test
    public void testEntrySetKeyInsensitive()
    {
        Map<String, Object> m = createSimpleMap();
        int one = 0;
        int three = 0;
        int five = 0;
        for (Map.Entry<String, Object> entry : m.entrySet())
        {
            if (entry.equals(new AbstractMap.SimpleEntry("one", "Two")))
            {
                one++;
            }
            if (entry.equals(new AbstractMap.SimpleEntry("thrEe", "Four")))
            {
                three++;
            }
            if (entry.equals(new AbstractMap.SimpleEntry("FIVE", "Six")))
            {
                five++;
            }
        }

        assertEquals(1, one);
        assertEquals(1, three);
        assertEquals(1, five);
    }

    @Test
    public void testRetainAll2()
    {
        Map<String, String> oldMap = new CaseInsensitiveMap<>();
        Map<String, String> newMap = new CaseInsensitiveMap<>();

        oldMap.put("foo", null);
        oldMap.put("bar", null);
        newMap.put("foo", null);
        newMap.put("bar", null);
        newMap.put("qux", null);
        Set<String> oldKeys = oldMap.keySet();
        Set<String> newKeys = newMap.keySet();
        assertTrue(newKeys.retainAll(oldKeys));
    }

    @Test
    public void testRetainAll3()
    {
        Map<String, String> oldMap = new CaseInsensitiveMap<>();
        Map<String, String> newMap = new CaseInsensitiveMap<>();

        oldMap.put("foo", null);
        oldMap.put("bar", null);
        newMap.put("foo", null);
        newMap.put("bar", null);
        Set<String> oldKeys = oldMap.keySet();
        Set<String> newKeys = newMap.keySet();
        assertFalse(newKeys.retainAll(oldKeys));
    }

    @Test
    public void testRemoveAll2()
    {
        Map<String, String> oldMap = new CaseInsensitiveMap<>();
        Map<String, String> newMap = new CaseInsensitiveMap<>();

        oldMap.put("bart", null);
        oldMap.put("qux", null);
        newMap.put("foo", null);
        newMap.put("bar", null);
        newMap.put("qux", null);
        Set<String> oldKeys = oldMap.keySet();
        Set<String> newKeys = newMap.keySet();
        boolean ret = newKeys.removeAll(oldKeys);
        assertTrue(ret);
    }

    @Test
    public void testAgainstUnmodifiableMap()
    {
        Map<String, String> oldMeta = new CaseInsensitiveMap<>();
        oldMeta.put("foo", "baz");
        oldMeta = Collections.unmodifiableMap(oldMeta);
        oldMeta.keySet();
        Map<String, String> newMeta = new CaseInsensitiveMap<>();
        newMeta.put("foo", "baz");
        newMeta.put("bar", "qux");
        newMeta = Collections.unmodifiableMap(newMeta);

        Set<String> oldKeys = new CaseInsensitiveSet<>(oldMeta.keySet());
        Set<String> sameKeys = new CaseInsensitiveSet<>(newMeta.keySet());
        sameKeys.retainAll(oldKeys);
    }

    @Test
    public void testSetValueApiOnEntrySet()
    {
        Map<String, String> map = new CaseInsensitiveMap();
        map.put("One", "Two");
        map.put("Three", "Four");
        map.put("Five", "Six");
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            if ("Three".equals(entry.getKey()))
            {   // Make sure this 'writes thru' to the underlying map's value.
                entry.setValue("~3");
            }
        }
        assertEquals("~3", map.get("Three"));
    }

    @Test
    public void testWrappedTreeMap()
    {
        Map map = new CaseInsensitiveMap(new TreeMap());
        map.put("z", "zulu");
        map.put("J", "juliet");
        map.put("a", "alpha");
        assert map.size() == 3;
        Iterator i = map.keySet().iterator();
        assert "a" == i.next();
        assert "J" == i.next();
        assert "z" == i.next();
        assert map.containsKey("A");
        assert map.containsKey("j");
        assert map.containsKey("Z");

        assert ((CaseInsensitiveMap)map).getWrappedMap() instanceof TreeMap;
    }

    @Test
    public void testWrappedTreeMapNotAllowsNull()
    {
        try
        {
            Map map = new CaseInsensitiveMap(new TreeMap());
            map.put(null, "not allowed");
            fail();
        }
        catch (NullPointerException ignored)
        { }
    }

    @Test
    public void testWrappedConcurrentHashMap()
    {
        Map map = new CaseInsensitiveMap(new ConcurrentHashMap());
        map.put("z", "zulu");
        map.put("J", "juliet");
        map.put("a", "alpha");
        assert map.size() == 3;
        assert map.containsKey("A");
        assert map.containsKey("j");
        assert map.containsKey("Z");

        assert ((CaseInsensitiveMap)map).getWrappedMap() instanceof ConcurrentHashMap;
    }

    @Test
    public void testWrappedConcurrentMapNotAllowsNull()
    {
        try
        {
            Map map = new CaseInsensitiveMap(new ConcurrentHashMap());
            map.put(null, "not allowed");
            fail();
        }
        catch (NullPointerException ignored)
        { }
    }

    @Test
    public void testWrappedMapKeyTypes()
    {
        CaseInsensitiveMap<String, Object> map = new CaseInsensitiveMap<>();
        map.put("Alpha", 1);
        map.put("alpha", 2);
        map.put("alPHA", 3);

        assert map.size() == 1;
        assert map.containsKey("Alpha");
        assert map.containsKey("alpha");
        assert map.containsKey("alPHA");

        Map check = map.getWrappedMap();
        assert check.keySet().size() == 1;
        assert check.keySet().iterator().next() instanceof CaseInsensitiveMap.CaseInsensitiveString;
    }

    @Test
    public void testUnmodifiableMap()
    {
        Map junkMap = new ConcurrentHashMap();
        junkMap.put("z", "zulu");
        junkMap.put("J", "juliet");
        junkMap.put("a", "alpha");
        Map map = new CaseInsensitiveMap(Collections.unmodifiableMap(junkMap));
        assert map.size() == 3;
        assert map.containsKey("A");
        assert map.containsKey("j");
        assert map.containsKey("Z");
        map.put("h", "hotel");      // modifiable allowed on the CaseInsensitiveMap
    }

    @Test
    public void testWeakHashMap()
    {
        Map map = new CaseInsensitiveMap(new WeakHashMap());
        map.put("z", "zulu");
        map.put("J", "juliet");
        map.put("a", "alpha");
        assert map.size() == 3;
        assert map.containsKey("A");
        assert map.containsKey("j");
        assert map.containsKey("Z");

        assert ((CaseInsensitiveMap)map).getWrappedMap() instanceof WeakHashMap;
    }

    @Test
    public void testWrappedMap()
    {
        Map linked = new LinkedHashMap();
        linked.put("key1", 1);
        linked.put("key2", 2);
        linked.put("key3", 3);
        CaseInsensitiveMap caseInsensitive = new CaseInsensitiveMap(linked);
        Set<String> newKeys = new LinkedHashSet();
        newKeys.add("key4");
        newKeys.add("key5");
        int newValue = 4;

        for (String key : newKeys)
        {
            caseInsensitive.put(key, newValue++);
        }

        Iterator i = caseInsensitive.keySet().iterator();
        assertEquals(i.next(), "key1");
        assertEquals(i.next(), "key2");
        assertEquals(i.next(), "key3");
        assertEquals(i.next(), "key4");
        assertEquals(i.next(), "key5");
    }

    @Test
    public void testNotRecreatingCaseInsensitiveStrings()
    {
        Map map = new CaseInsensitiveMap();
        map.put("dog", "eddie");

        // copy 1st map
        Map newMap = new CaseInsensitiveMap(map);

        CaseInsensitiveMap.CaseInsensitiveEntry entry1 = (CaseInsensitiveMap.CaseInsensitiveEntry) map.entrySet().iterator().next();
        CaseInsensitiveMap.CaseInsensitiveEntry entry2 = (CaseInsensitiveMap.CaseInsensitiveEntry) newMap.entrySet().iterator().next();

        assertTrue(entry1.getOriginalKey() == entry2.getOriginalKey());
    }

    @Test
    public void testPutAllOfNonCaseInsensitiveMap()
    {
        Map nonCi = new HashMap();
        nonCi.put("Foo", "bar");
        nonCi.put("baz", "qux");

        Map ci = new CaseInsensitiveMap();
        ci.putAll(nonCi);

        assertTrue(ci.containsKey("foo"));
        assertTrue(ci.containsKey("Baz"));
    }

    @Test
    public void testNotRecreatingCaseInsensitiveStringsUsingTrackingMap()
    {
        Map map = new CaseInsensitiveMap();
        map.put("dog", "eddie");
        map = new TrackingMap(map);

        // copy 1st map
        Map newMap = new CaseInsensitiveMap(map);

        CaseInsensitiveMap.CaseInsensitiveEntry entry1 = (CaseInsensitiveMap.CaseInsensitiveEntry) map.entrySet().iterator().next();
        CaseInsensitiveMap.CaseInsensitiveEntry entry2 = (CaseInsensitiveMap.CaseInsensitiveEntry) newMap.entrySet().iterator().next();

        assertTrue(entry1.getOriginalKey() == entry2.getOriginalKey());
    }

    @Test
    public void testEntrySetIsEmpty()
    {
        Map map = createSimpleMap();
        Set entries = map.entrySet();
        assert !entries.isEmpty();
    }

    @Test
    public void testPlus()
    {
        CaseInsensitiveMap x = new CaseInsensitiveMap();
        Map y = new HashMap();

        try
        {
            x.plus(y);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
        }
    }

    @Test
    public void testMinus()
    {
        CaseInsensitiveMap x = new CaseInsensitiveMap();
        Map y = new HashMap();

        try
        {
            x.minus(y);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
        }
    }

    @Test
    public void testPutObject()
    {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.putObject(1L, 1L);
        map.putObject("hi", "ho");
        Object x = map.putObject("hi", "hi");
        assert x == "ho";
        map.putObject(Boolean.TRUE, Boolean.TRUE);
        String str = "hello";
        CaseInsensitiveMap.CaseInsensitiveString ciStr = new CaseInsensitiveMap.CaseInsensitiveString(str);
        map.putObject(ciStr, str);
        assert map.get(str) == str;
        assert 1L == ((Number)map.get(1L)).longValue();
        assert Boolean.TRUE == map.get(true);
    }

    @Test
    public void testTwoMapConstructor()
    {
        Map real = new HashMap();
        real.put("z", 26);
        real.put("y", 25);
        real.put("m", 13);
        real.put("d", 4);
        real.put("c", 3);
        real.put("b", 2);
        real.put("a", 1);

        Map backingMap = new TreeMap();
        CaseInsensitiveMap ciMap = new CaseInsensitiveMap(real, backingMap);
        assert ciMap.size() == real.size();
        assert ciMap.containsKey("Z");
        assert ciMap.containsKey("A");
        assert ciMap.getWrappedMap() instanceof TreeMap;
        assert ciMap.getWrappedMap() == backingMap;
    }

    @Test
    public void testCaseInsensitiveStringConstructor()
    {
        CaseInsensitiveMap.CaseInsensitiveString ciString = new CaseInsensitiveMap.CaseInsensitiveString("John");
        assert ciString.equals("JOHN");
        assert ciString.equals("john");
        assert ciString.hashCode() == "John".toLowerCase().hashCode();
        assert ciString.compareTo("JOHN") == 0;
        assert ciString.compareTo("john") == 0;
        assert ciString.compareTo("alpha") > 0;
        assert ciString.compareTo("ALPHA") > 0;
        assert ciString.compareTo("theta") < 0;
        assert ciString.compareTo("THETA") < 0;
        assert ciString.toString().equals("John");
    }

    @Test
    public void testHeterogeneousMap()
    {
        Map<Object, Object> ciMap = new CaseInsensitiveMap<>();
        ciMap.put(1.0d, "foo");
        ciMap.put("Key", "bar");
        ciMap.put(true, "baz");

        assert ciMap.get(1.0d) == "foo";
        assert ciMap.get("Key") == "bar";
        assert ciMap.get(true) == "baz";

        assert ciMap.remove(true) == "baz";
        assert ciMap.size() == 2;
        assert ciMap.remove(1.0d) == "foo";
        assert ciMap.size() == 1;
        assert ciMap.remove("Key") == "bar";
        assert ciMap.size() == 0;
    }

    @Test
    public void testCaseInsensitiveString()
    {
        CaseInsensitiveMap.CaseInsensitiveString ciString = new CaseInsensitiveMap.CaseInsensitiveString("foo");
        assert ciString.equals(ciString);
        assert ciString.compareTo(1.5d) < 0;

        CaseInsensitiveMap.CaseInsensitiveString ciString2 = new CaseInsensitiveMap.CaseInsensitiveString("bar");
        assert !ciString.equals(ciString2);
    }

    @Test
    public void testCaseInsensitiveStringHashcodeCollision()
    {
        CaseInsensitiveMap.CaseInsensitiveString ciString = new CaseInsensitiveMap.CaseInsensitiveString("f608607");
        CaseInsensitiveMap.CaseInsensitiveString ciString2 = new CaseInsensitiveMap.CaseInsensitiveString("f16010070");
        assert ciString.hashCode() == ciString2.hashCode();
        assert !ciString.equals(ciString2);
    }

    @Ignore
    @Test
    public void testGenHash()
    {
        final String TEXT = "was stored earlier had the same hash as";
        HashMap<Integer, CaseInsensitiveMap.CaseInsensitiveString> hs = new HashMap<>();
        long t1 = System.currentTimeMillis();
        long t2 = System.currentTimeMillis();
        for (long l = 0; l < Long.MAX_VALUE; l++)
        {
            CaseInsensitiveMap.CaseInsensitiveString key = new CaseInsensitiveMap.CaseInsensitiveString("f" + l);
            if (hs.containsKey(key.hashCode()))
            {
                System.out.println("'" + hs.get(key.hashCode()) + "' " + TEXT + " '" + key + "'");
                break;
            }
            else
            {
                hs.put(key.hashCode(),key);
            }

            t2 = System.currentTimeMillis();

            if (t2 - t1 > 10000)
            {
                t1 = System.currentTimeMillis();
                System.out.println("10 seconds gone! size is:"+hs.size());
            }
        }
        System.out.println("Done");
    }

    @Test
    public void testConcurrentSkipListMap()
    {
        ConcurrentMap<String, Object> map = new ConcurrentSkipListMap<>();
        map.put("key1", "foo");
        map.put("key2", "bar");
        map.put("key3", "baz");
        map.put("key4", "qux");
        CaseInsensitiveMap<String, Object> ciMap = new CaseInsensitiveMap<>(map);
        assert ciMap.get("KEY1") == "foo";
        assert ciMap.get("KEY2") == "bar";
        assert ciMap.get("KEY3") == "baz";
        assert ciMap.get("KEY4") == "qux";
    }

    // Used only during development right now
    @Ignore
    @Test
    public void testPerformance()
    {
        Map<String, String> map = new CaseInsensitiveMap<>();
        Random random = new Random();

        long start = System.nanoTime();

        for (int i=0; i < 10000; i++)
        {
            String key = StringUtilities.getRandomString(random, 1, 10);
            String value = StringUtilities.getRandomString(random, 1, 10);
            map.put(key, value);
        }

        long stop = System.nanoTime();
        System.out.println((stop - start) / 1000000);

        start = System.nanoTime();

        for (int i=0; i < 100000; i++)
        {
            Map copy = new CaseInsensitiveMap<>(map);
        }

        stop = System.nanoTime();

        System.out.println((stop - start) / 1000000);
    }

    // ---------------------------------------------------


    private CaseInsensitiveMap<String, Object> createSimpleMap()
    {
        CaseInsensitiveMap<String, Object> stringMap = new CaseInsensitiveMap<String, Object>();
        stringMap.put("One", "Two");
        stringMap.put("Three", "Four");
        stringMap.put("Five", "Six");
        return stringMap;
    }

    private Map.Entry getEntry(final Object key, final Object value)
    {
        return new Map.Entry()
        {
            Object myValue = value;

            public Object getKey()
            {
                return key;
            }

            public Object getValue()
            {
                return value;
            }

            public Object setValue(Object value)
            {
                Object save = myValue;
                myValue = value;
                return save;
            }
        };
    }
}
