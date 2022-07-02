package io.johnsonlee.template.booster

import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService

@AutoService(ClassTransformer::class)
class TemplateClassTransformer : ClassTransformer {
}