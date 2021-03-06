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

import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KParameter
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

/**
 * Factory class for creating [BoundMethod].
 * This is the recommended way for most people
 * to create their [BoundMethod] instances.
 * Creating instances on your own is not advised
 */
object MethodBinder {
    /**
     * Default [SyntaxGenerator] used by [BoundMethod]s when generating syntax.
     */
    @JvmStatic
    val generator = SyntaxGeneratorContainer(
            NameSyntaxGenerator(),
            IndexSyntaxGenerator(),
            AutoIndexSyntaxGenerator(),
            KotlinDefaultValueSyntaxGenerator()/*,
            Disabled by default as it might cause
            unexpected results
            ParamNameSyntaxGenerator()*/
                                            )

    /**
     * Binds the given [method]
     *
     * @param owner required only when the method is instanceless (e.g. `String::substring`).
     *              When it has a instance bound though (e.g. `"Something"::substring`) the parameter is ignored
     */
    @JvmStatic
    fun bindMethod(method: KCallable<*>, owner: Any? = null): BoundMethod
            = CallableBoundMethod(method.withInstanceIfNeeded(owner))

    /**
     * Binds the given [method]. Intended to be used with Java.
     *
     * @param defaultValues when input isn't specified this will be used by default (port of Kotlin's default parameters)
     */
    @JvmStatic
    @JvmOverloads
    fun bindMethod(owner: Any, method: Method, vararg defaultValues: Any? = emptyArray()): BoundMethod
            = CallableBoundMethod((method.kotlinFunction ?:
            throw IllegalArgumentException("Can't convert method ($method) to a kotlin function"))
                                          .withInstance(owner),
                                  generator.withDefaultValues(defaultValues))

    private fun SyntaxGeneratorContainer.withDefaultValues(defaultValues: Array<out Any?>): SyntaxGeneratorContainer =
            if (defaultValues.isNotEmpty())
                this + JavaDefaultValueSyntaxGenerator(defaultValues)
            else
                this

    /**
     * Searches for a method with the given [name] inside that class's methods. Returns null on failure (method not found)
     * Intended to be used with Java.
     *
     * @param defaultValues when input isn't specified this will be used by default (port of Kotlin's default parameters)
     */
    @JvmStatic
    @JvmOverloads
    fun bindMethod(owner: Any, name: String, vararg defaultValues: Any? = emptyArray()): BoundMethod {
        val func = owner::class.java.declaredMethods.let {
            it.firstOrNull { it.annotations.filterIsInstance(BindMethod::class.java).getOrNull(0)?.name == name } ?:
                    it.firstOrNull { it.name == name }
        } ?: throw IllegalArgumentException(
                "Couldn't find method with name $name in class ${owner::class.java.simpleName}")

        return bindMethod(owner, func, defaultValues)
    }

    private fun <T> KCallable<T>.withInstanceIfNeeded(owner: Any?): KCallable<T> =
            if (instanceParameter ?: extensionReceiverParameter == null) this
            else this.withInstance(
                    owner ?: throw IllegalArgumentException("function has instance param but owner is null"))
}


private class KCallableWithInstance<out T>(private val func: KCallable<T>,
                                           private val instance: Any
                                          ) : KCallable<T> by func {
    private val instanceParam = func.instanceParameter ?:
            func.extensionReceiverParameter ?:
            throw IllegalArgumentException("Function ($func) already has a instance bound")

    init {
        val instanceParamType = instanceParam.type.jvmErasure
        if (!instance::class.isSubclassOf(instanceParamType))
            throw IllegalArgumentException(
                    "Provided instance (${instance::class.qualifiedName}) isn't an subclass of " +
                            "instance param's value's class (${instanceParamType::class.qualifiedName})")
    }

    override fun call(vararg args: Any?): T
            = func.call(instance, *args)


    override fun callBy(args: Map<KParameter, Any?>): T
            = func.callBy(args + (instanceParam to instance))

    override val parameters = func.parameters.filter { it != instanceParam }
}

private fun <T> KCallable<T>.withInstance(instance: Any): KCallable<T>
        = KCallableWithInstance(this, instance)