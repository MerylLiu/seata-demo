package com.example.common;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.util.Objects;

/**
 * @Author : Meryl
 * @Description:
 * @Date: Created in 2020/1/4
 * @Modify by :
 */
public class DistributedTransImportSelector implements ImportSelector {
    public static boolean enabledTransaction = false;

    @Override
    @NonNull
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {

        boolean enabled = Boolean.valueOf(
                Objects.requireNonNull(
                        importingClassMetadata.getAnnotationAttributes(DistTransApplication.class.getName())
                ).get("enabled").toString());

        enabledTransaction = enabled;

        return new String[0];
    }
}
