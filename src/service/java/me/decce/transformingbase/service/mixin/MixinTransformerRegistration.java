package me.decce.transformingbase.service.mixin;

import me.decce.transformingbase.service.transform.CommonTransformer;
import me.decce.transformingbase.service.transform.ReflectionUtil;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class MixinTransformerRegistration {
    @SuppressWarnings("unchecked")
    public static void register(Object transformer) {
        try {
            var processorField = transformer.getClass().getDeclaredField("processor");
            processorField.setAccessible(true);
            var processor = processorField.get(transformer);
            var coprocessorsField = processor.getClass().getDeclaredField("coprocessors");
            coprocessorsField.setAccessible(true);
            var coprocessors = (List<Object>) coprocessorsField.get(processor);
            coprocessors.add(createCoprocessor(MixinEnvironment.class.getClassLoader(), CommonTransformer::process));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register the Jasione Mixin coprocessor", e);
        }
    }

    private static Object createCoprocessor(ClassLoader classLoader, Predicate<ClassNode> predicate)
            throws ReflectiveOperationException {
        String className = "org.spongepowered.asm.mixin.transformer.JasioneMixinCoprocessor";
        byte[] bytes;
        try (var resource = MixinTransformerRegistration.class.getResourceAsStream("/" + className.replace('.', '/') + ".class")) {
            bytes = Objects.requireNonNull(resource).readAllBytes();
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        var coprocessorClass = ReflectionUtil.defineClass(classLoader, className, bytes);
        return coprocessorClass.getConstructor(Predicate.class).newInstance(predicate);
    }
}
