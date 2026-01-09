package net.buildtheearth.terraplusplus.util.netty;

import io.netty.resolver.InetNameResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.SocketUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Equivalent to {@link io.netty.resolver.DefaultNameResolver}, but performs the blocking name lookups on a separate {@link Executor}.
 *
 * @author DaPorkchop_
 * @see io.netty.resolver.DefaultNameResolver
 */
public class AsyncDefaultNameResolver extends InetNameResolver {
    private final Executor resolveExecutor;

    /**
     * @param executor        the {@link EventExecutor} which is used to notify the listeners of the {@link Future} returned by {@link #resolve(String)}
     * @param resolveExecutor the {@link Executor} which is used to perform name lookups
     */
    public AsyncDefaultNameResolver(EventExecutor executor, Executor resolveExecutor) {
        super(executor);
        this.resolveExecutor = Objects.requireNonNull(resolveExecutor, "resolveExecutor");
    }

    @Override
    protected void doResolve(String inetHost, Promise<InetAddress> promise) throws Exception {
        this.resolveExecutor.execute(() -> {
            try {
                promise.setSuccess(SocketUtils.addressByName(inetHost));
            } catch (UnknownHostException e) {
                promise.setFailure(e);
            }
        });
    }

    @Override
    protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) throws Exception {
        this.resolveExecutor.execute(() -> {
            try {
                promise.setSuccess(Arrays.asList(SocketUtils.allAddressesByName(inetHost)));
            } catch (UnknownHostException e) {
                promise.setFailure(e);
            }
        });
    }
}
