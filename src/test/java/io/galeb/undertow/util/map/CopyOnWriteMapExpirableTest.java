package io.galeb.undertow.util.map;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CopyOnWriteMapExpirableTest {

    private static final long DEFAULT_TTL = 1000L;

    private Map<String, Integer> copyOnWriteMapExpirable;


    @Before
    public void setUp() {
        copyOnWriteMapExpirable = new CopyOnWriteMapExpirable<>(DEFAULT_TTL);
    }

    @After
    public void cleanUp() {
        copyOnWriteMapExpirable = null;
    }

    @Test
    public void putAndGetTest() {
        copyOnWriteMapExpirable.put(toString(), Integer.MIN_VALUE);
        assert(copyOnWriteMapExpirable.get(toString())).equals(Integer.MIN_VALUE);
    }

    @Test
    public void sizeTest() {
        copyOnWriteMapExpirable.put(toString(), Integer.MIN_VALUE);
        assert(Integer.valueOf(copyOnWriteMapExpirable.size())).equals(1);
    }

    @Test
    public void isEmptyTest() {
        assert(Boolean.valueOf(copyOnWriteMapExpirable.isEmpty())).equals(true);
    }

    @Test
    public void containsKeyTest() {
        copyOnWriteMapExpirable.put(toString(), Integer.MIN_VALUE);
        assert(Boolean.valueOf(copyOnWriteMapExpirable.containsKey(toString()))).equals(true);
    }

    @Test
    public void containsValueTest() {
        copyOnWriteMapExpirable.put(toString(), Integer.MIN_VALUE);
        assert(Boolean.valueOf(copyOnWriteMapExpirable.containsValue(Integer.MIN_VALUE))).equals(true);
    }

    @Test
    public void removeTest() {
        copyOnWriteMapExpirable.put(toString(), Integer.MIN_VALUE);
        assert(copyOnWriteMapExpirable.get(toString())).equals(Integer.MIN_VALUE);
        copyOnWriteMapExpirable.remove(toString());
        assert(Boolean.valueOf(copyOnWriteMapExpirable.isEmpty())).equals(true);
    }

    @Test
    public void putAllTest() {
        Map<String, Integer> mapTemp = new HashMap<>();
        for (int x=0; x<10; x++) {
            mapTemp.put(Integer.toString(x), x);
        }
        copyOnWriteMapExpirable.putAll(mapTemp);
        for (int x=0; x<10; x++) {
            assert(copyOnWriteMapExpirable.get(Integer.toString(x))).equals(x);
        }
    }

    @Test
    public void clearTest() {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        copyOnWriteMapExpirable.clear();
        assert(Boolean.valueOf(copyOnWriteMapExpirable.isEmpty())).equals(true);
    }

    @Test
    public void keySetTest() {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        assert(Integer.valueOf(copyOnWriteMapExpirable.keySet().size())).equals(10);
    }

    @Test
    public void valuesTest() {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        assert(Integer.valueOf(copyOnWriteMapExpirable.values().size())).equals(10);
    }

    @Test
    public void entrySetTest() {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        assert(Integer.valueOf(copyOnWriteMapExpirable.entrySet().size())).equals(10);
    }

    @Test
    public void clearExpiredTest() throws InterruptedException {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        Thread.sleep(DEFAULT_TTL);
        ((CopyOnWriteMapExpirable<String, Integer>) copyOnWriteMapExpirable).clearExpired();
        assert(Boolean.valueOf(copyOnWriteMapExpirable.isEmpty())).equals(true);
    }

    @Test
    public void renewAllTest() throws InterruptedException {
        for (int x=0; x<10; x++) {
            copyOnWriteMapExpirable.put(Integer.toString(x), x);
        }
        Thread.sleep(DEFAULT_TTL);
        ((CopyOnWriteMapExpirable<String, Integer>) copyOnWriteMapExpirable).renewAll();
        ((CopyOnWriteMapExpirable<String, Integer>) copyOnWriteMapExpirable).clearExpired();
        assert(Integer.valueOf(copyOnWriteMapExpirable.entrySet().size())).equals(10);
    }

}
