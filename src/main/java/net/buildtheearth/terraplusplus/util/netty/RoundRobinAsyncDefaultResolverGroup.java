package net.buildtheearth.terraplusplus.util.netty;

import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.RoundRobinInetAddressResolver;
import io.netty.util.concurrent.EventExecutor;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * An {@link AddressResolverGroup} of {@link AsyncDefaultNameResolver}s that supports random selection of destination addresses if
 * multiple are provided by the nameserver. This is ideal for use in applications that use a pool of connections, for
 * which connecting to a single resolved address would be inefficient.
 *
 * @author DaPorkchop_
 * @see io.netty.resolver.dns.RoundRobinDnsAddressResolverGroup
 */
public class RoundRobinAsyncDefaultResolverGroup extends AddressResolverGroup<InetSocketAddress> {
    private final Executor lookupExecutor;

    /**
     * @param lookupExecutor the {@link Executor} which is used to perform name lookups
     */
    public RoundRobinAsyncDefaultResolverGroup(Executor lookupExecutor) {
        this.lookupExecutor = Objects.requireNonNull(lookupExecutor, "lookupExecutor");
    }

    @Override
    protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) throws Exception {
        return new RoundRobinInetAddressResolver(executor, new AsyncDefaultNameResolver(executor, this.lookupExecutor)).asAddressResolver();
    }
}
