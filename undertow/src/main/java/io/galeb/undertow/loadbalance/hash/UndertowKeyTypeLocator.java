package io.galeb.undertow.loadbalance.hash;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import io.galeb.core.loadbalance.hash.ExtractableKey;
import io.galeb.core.loadbalance.hash.KeyTypeLocator;
import io.galeb.undertow.extractable.UndertowCookie;
import io.galeb.undertow.extractable.UndertowSourceIP;
import io.galeb.undertow.extractable.UndertowURI;

public class UndertowKeyTypeLocator implements KeyTypeLocator {

    private static enum UndertowKeyType {
        SOURCE_IP(KeyType.SOURCE_IP.toString(), new UndertowSourceIP()),
        COOKIE(KeyType.COOKIE.toString(),       new UndertowCookie()),
        URI(KeyType.URI.toString(),             new UndertowURI());

        final String simpleName;
        final ExtractableKey extractableKey;
        private UndertowKeyType(String simpleName, ExtractableKey extractableKey) {
            this.simpleName = simpleName;
            this.extractableKey = extractableKey;
        }

        public ExtractableKey getExtractableKey() {
            return extractableKey;
        }

        @Override
        public String toString() {
            return simpleName;
        }
    }

    public static final KeyTypeLocator INSTANCE = new UndertowKeyTypeLocator();

    private final Map<String, ExtractableKey> mapOfExtractableKey = new HashMap<>();

    private UndertowKeyTypeLocator() {
        EnumSet.allOf(UndertowKeyType.class).forEach(keyType -> {
            mapOfExtractableKey.put(keyType.toString(), keyType.getExtractableKey());
        });
    }

    @Override
    public ExtractableKey getKey(String keyType) {
        final ExtractableKey extractableKey = mapOfExtractableKey.get(keyType);
        return extractableKey!= null ? extractableKey : ExtractableKey.NULL;
    }

}
