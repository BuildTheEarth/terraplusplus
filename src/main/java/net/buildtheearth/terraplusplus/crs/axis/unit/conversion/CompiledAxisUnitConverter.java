package net.buildtheearth.terraplusplus.crs.axis.unit.conversion;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.crs.axis.unit.AxisUnitConverter;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.unsafe.PUnsafe;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * An implementation of a {@link AxisUnitConverter} which employs runtime bytecode generation in order to aggressively optimize arbitrary conversions.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CompiledAxisUnitConverter implements AxisUnitConverter {
    private static final LoadingCache<AxisUnitConverter, CompiledAxisUnitConverter> COMPILED_CONVERTERS_CACHE = CacheBuilder.newBuilder()
            .weakKeys()
            .weakValues()
            .build(CacheLoader.from(CompiledAxisUnitConverter::compile0));

    public static AxisUnitConverter compile(@NonNull AxisUnitConverter converter) {
        if (converter instanceof CompiledAxisUnitConverter) { //already compiled
            return converter;
        }

        converter = converter.simplify().intern();

        if (converter.isIdentity()) { //identity conversion doesn't need to be compiled
            return AxisUnitConverterIdentity.instance().intern();
        } else if (converter instanceof AxisUnitConverterAdd || converter instanceof AxisUnitConverterMultiply) { //these are simple enough that compiling them won't provide any benefit
            return converter;
        }

        return COMPILED_CONVERTERS_CACHE.getUnchecked(converter);
    }

    //assumes the converter has already been simplified and interned
    @SneakyThrows
    private static CompiledAxisUnitConverter compile0(@NonNull AxisUnitConverter converter) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        String internalName = getInternalName(CompiledAxisUnitConverter.class) + "$Impl";

        writer.visit(V1_8, ACC_PUBLIC | ACC_FINAL, internalName, null, getInternalName(CompiledAxisUnitConverter.class), null);

        {
            MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "<init>", getMethodDescriptor(VOID_TYPE, getType(AxisUnitConverter.class)), null, null);
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, getInternalName(CompiledAxisUnitConverter.class), "<init>", getMethodDescriptor(VOID_TYPE, getType(AxisUnitConverter.class)), false);
            mv.visitInsn(RETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        {
            MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "convert", getMethodDescriptor(DOUBLE_TYPE, DOUBLE_TYPE), null, null);
            mv.visitCode();

            mv.visitVarInsn(DLOAD, 1);
            compileConvertScalar(mv, 3, converter);
            mv.visitInsn(DRETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        writer.visitEnd();

        byte[] arr = writer.toByteArray();
        Class<?> clazz = PUnsafe.defineClass(internalName, arr, 0, arr.length, new ClassLoader(CompiledAxisUnitConverter.class.getClassLoader()) {}, null);

        return (CompiledAxisUnitConverter) clazz.getDeclaredConstructor(AxisUnitConverter.class).newInstance(converter);
    }

    @SneakyThrows
    private static void compileConvertScalar(@NonNull MethodVisitor mv, int lvtIndexAllocator, @NonNull AxisUnitConverter converter) {
        if (converter instanceof AxisUnitConverterAdd) {
            mv.visitLdcInsn(((AxisUnitConverterAdd) converter).offset());
            mv.visitInsn(DADD);
        } else if (converter instanceof AxisUnitConverterMultiply) {
            mv.visitLdcInsn(((AxisUnitConverterMultiply) converter).factor());
            mv.visitInsn(DMUL);
        } else if (converter instanceof AxisUnitConverterSequence) {
            for (AxisUnitConverter next : ((AxisUnitConverterSequence) converter).converters()) {
                compileConvertScalar(mv, lvtIndexAllocator, next);
            }
        } else {
            throw new IllegalArgumentException(PorkUtil.className(converter));
        }
    }

    @NonNull
    private final AxisUnitConverter original;

    @Override
    public final boolean isIdentity() {
        //identity conversions are never compiled
        return false;
    }

    @Override
    public abstract double convert(double value);

    @Override
    public final AxisUnitConverter inverse() {
        return this.original.inverse();
    }

    @Override
    public final AxisUnitConverter simplify() {
        //the converter has already been maximally simplified
        return this;
    }

    @Override
    public final AxisUnitConverter andThen(@NonNull AxisUnitConverter next) {
        return this.original.andThen(next);
    }

    @Override
    public final AxisUnitConverter intern() {
        return this;
    }
}
