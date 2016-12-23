package io.galeb.core.loadbalance;

import io.galeb.core.util.consistenthash.HashAlgorithm;

import java.net.URI;
import java.util.Objects;

public class ExpirableURI {
    private final URI uri;
    private long statusTime;
    private boolean quarantine;
    private final HashAlgorithm hashAlgorithm = new HashAlgorithm(HashAlgorithm.HashType.MD5);

    public ExpirableURI(final URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    public long getStatusTime() {
        return statusTime;
    }

    private void updateStatusTime() {
        this.statusTime = System.currentTimeMillis();
    }

    public boolean isQuarantine() {
        return quarantine;
    }

    public void setQuarantine(boolean quarantine) {
        this.quarantine = quarantine;
        updateStatusTime();
    }

    public String uriHash(String uriStr) {
        return hashAlgorithm.hash(uriStr).asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpirableURI that = (ExpirableURI) o;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }
}
