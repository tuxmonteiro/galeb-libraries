package io.galeb.core.model;

import java.util.Map;

public interface FarmListener {

    void nodeLeftEvent(Map map);
    void nodeJoinedEvent(Map map);

}
