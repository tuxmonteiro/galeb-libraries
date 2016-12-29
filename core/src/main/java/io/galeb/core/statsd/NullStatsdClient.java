package io.galeb.core.statsd;

import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;

@Alternative
@Singleton
public class NullStatsdClient implements StatsdClient {

}