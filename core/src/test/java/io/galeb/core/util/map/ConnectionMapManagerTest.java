package io.galeb.core.util.map;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConnectionMapManagerTest {

    private final ConnectionMapManager connectionMapManager = ConnectionMapManager.INSTANCE;

    @Before
    public void setUp() {

    }

    @After
    public void cleanUp() {
        connectionMapManager.clear();
    }

    @Test
    public void reduceTest() {
        int z, x;
        z = x = 10;
        String uri = "UNDEF";
        connectionMapManager.putOnCounterMap(uri, x);
        assertThat(connectionMapManager.reduce().get(uri)).isEqualTo(z);
    }

}
