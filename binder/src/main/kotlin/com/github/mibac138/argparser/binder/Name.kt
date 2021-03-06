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

import com.github.mibac138.argparser.named.name
import com.github.mibac138.argparser.syntax.autoIndex
import com.github.mibac138.argparser.syntax.dsl.SyntaxElementDSL

/**
 * Annotation used to [automagically][CallableBoundMethod] generate [named][SyntaxElement.name] syntax elements
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Name(
        /**
         * The name you want the parameter to have
         */
        val value: String,
        /**
         * if true (auto) assigns an index to the parameter.
         * <b>Do not use this and [@Index][Index] simultaneously</b>
         */
        val ordered: Boolean = false)

/**
 * Generates [named][SyntaxElement.name] syntax by using the [`@Name`][Name] annotation
 */
class NameSyntaxGenerator : AnnotationBasedSyntaxGenerator<Name>(Name::class.java) {
    override fun generate(dsl: SyntaxElementDSL, annotation: Name) {
        dsl.name = annotation.value

        if (annotation.ordered)
            dsl.autoIndex()
    }
}