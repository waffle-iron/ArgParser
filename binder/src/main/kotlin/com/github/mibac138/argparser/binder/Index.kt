/*
 * Copyright (c) 2017 Michał Bączkowski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.mibac138.argparser.binder

import com.github.mibac138.argparser.syntax.autoIndex
import com.github.mibac138.argparser.syntax.dsl.SyntaxElementDSL
import com.github.mibac138.argparser.syntax.index
import kotlin.reflect.KParameter

/**
 * Annotation used to [automagically][CallableBoundMethod] generate [indexed][SyntaxElement.index] syntax elements
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Index(val value: Int)

/**
 * Use this to value to generate index using [autoIndex] instead of stating index
 */
const val AUTO: Int = -1

/**
 * Generates [indexed][SyntaxElement.index] syntax by using the [`@Index`][Index] annotation
 */
class IndexSyntaxGenerator : AnnotationBasedSyntaxGenerator<Index>(Index::class.java) {
    override fun generate(dsl: SyntaxElementDSL, annotation: Index) {
        if (annotation.value == AUTO)
            dsl.autoIndex()
        else
            dsl.index = annotation.value
    }
}

/**
 * Generates [indexed][SyntaxElement.index] syntax
 * for elements which have neither [`@Name`][Name]
 * nor [`@Index`][Index] annotations by using the
 * [auto index][autoIndex] function
 */
class AutoIndexSyntaxGenerator : SyntaxGenerator {
    override fun generate(dsl: SyntaxElementDSL, param: KParameter) {
        if (param.annotations.any(this::isInapplicable)) return

        dsl.autoIndex()
    }

    private fun isInapplicable(a: Annotation) = Name::class.java.isInstance(a) || Index::class.java.isInstance(a)
}