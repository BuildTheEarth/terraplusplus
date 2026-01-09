package net.buildtheearth.terraplusplus.util.netty;

import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.util.concurrent.EventExecutor;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * An {@link AddressResolverGroup} of {@link AsyncDefaultNameResolver}s.
 *
 * @author DaPorkchop_
 * @see io.netty.resolver.DefaultAddressResolverGroup
 */
public class AsyncDefaultResolverGroup extends AddressResolverGroup<InetSocketAddress> {
    private final Executor resolveExecutor;

    /**
     * @param resolveExecutor the {@link Executor} which is used to perform name lookups
     */
    public AsyncDefaultResolverGroup(Executor resolveExecutor) {
        this.resolveExecutor = Objects.requireNonNull(resolveExecutor, "resolveExecutor");
    }

    @Override
    protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) throws Exception {
        return new AsyncDefaultNameResolver(executor, this.resolveExecutor).asAddressResolver();
    }
}
