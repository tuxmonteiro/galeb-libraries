package io.galeb.core.cluster.ignite;

import java.util.Map;

public interface NodeEventListener {

    void nodeLeftEvent(Map map);
    void nodeJoinedEvent(Map map);

}
