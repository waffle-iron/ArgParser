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

package com.github.mibac138.argparser.parser

import com.github.mibac138.argparser.named.ArgumentMatcher
import com.github.mibac138.argparser.named.PatternArgumentMatcher
import com.github.mibac138.argparser.named.name
import com.github.mibac138.argparser.reader.ArgumentReader
import com.github.mibac138.argparser.reader.skipChar
import com.github.mibac138.argparser.syntax.*
import java.util.*
import java.util.regex.Pattern

/**
 * Can parse mixed syntax (i.e. with both named and unnamed elements)
 */
class MixedParserRegistryImpl : MixedParserRegistry {
    private val nameToParserMap: MutableMap<String, Parser> = HashMap()
    private val positionToParserMap: MutableMap<Int, Parser> = HashMap()
    override var matcher: ArgumentMatcher = PatternArgumentMatcher(Pattern.compile("--([a-zA-Z_0-9]+)(?:=|: ?)"))

    override fun getSupportedTypes(): Set<Class<*>> {
        val output = mutableSetOf<Class<*>>()
        nameToParserMap.values.map { it.getSupportedTypes() }.forEach { output += it }
        positionToParserMap.values.map { it.getSupportedTypes() }.forEach { output += it }

        return output
    }

    override fun parse(input: ArgumentReader, syntax: SyntaxElement<*>): Map<String?, *> {
        val named = mutableMapOf<String?, Any?>()
        val unnamed = mutableListOf<Any?>()
        // LinkedList has O(1) add, remove and Iterator.next (the only used methods here)
        val unprocessedSyntax = LinkedList(syntax.content())

        var index = 0
        for (i in 0 until syntax.getSize()) {

            input.skipChar(' ')
            val matched = matcher.match(input)

            if (matched == null) {
                val element = syntax.findElementById(index)
                val parser = positionToParserMap[index] ?: element.parser
                val parsed = parseElementOrDefault(input, element, parser)

                unnamed += parsed
                unprocessedSyntax -= element
                index++
            } else {
                val element = syntax.findElementByName(matched.name)
                val parser = getParserForElement(element)
                val parsed = parseElement(matched.value, element, parser)

                named[matched.name] = parsed
                unprocessedSyntax -= element
            }
        }

        for (element in unprocessedSyntax) {
            val name = element.name

            if (name == null) {
                val parser = positionToParserMap[index] ?: element.parser
                val parsed = parseElementOrDefault(input, element, parser)

                unnamed += parsed
                index++
            } else {
                val parser = getParserForElement(element)
                val parsed = parseElement(input, element, parser)

                named[name] = parsed
            }
        }

        named[null] = unnamed
        return named
    }

    private fun parseElementOrDefault(input: ArgumentReader, element: SyntaxElement<*>, parser: Parser?): Any?
            = when (parser) {
        null -> element.defaultValue
        else -> parseElement(input, element, parser)
    }

    private fun parseElement(input: ArgumentReader, element: SyntaxElement<*>, parser: Parser): Any? {
        input.skipChar(' ')
        input.mark()
        val parsed: Any?
        try {
            parsed = parser.parse(input, element)
        } catch (e: Exception) {
            input.reset()
            throw e
        }

        input.removeMark()
        return parsed
    }

    override fun registerParser(parser: Parser, name: String) {
        nameToParserMap[name] = parser
    }

    override fun removeParser(name: String) {
        nameToParserMap.remove(name)
    }

    override fun registerParser(parser: Parser) {
        positionToParserMap[positionToParserMap.size] = parser
    }

    override fun registerParser(parser: Parser, position: Int) {
        positionToParserMap[position] = parser
    }

    override fun removeParser(position: Int) {
        positionToParserMap.remove(position)
    }

    override fun removeParser(parser: Parser)
            = throw UnsupportedOperationException()

    override fun removeAllParsers() {
        nameToParserMap.clear()
        positionToParserMap.clear()
    }

    private fun getParserForElement(element: SyntaxElement<*>)
            = element.parser ?: getParserForName(element.name ?:
            throw IllegalArgumentException())

    private fun getParserForName(name: String): Parser {
        val parser = nameToParserMap[name]
        if (parser != null)
            return parser

        throw IllegalArgumentException("Couldn't find parser for name '$name'")
    }

    private fun SyntaxElement<*>.findElementById(id: Int): SyntaxElement<*> {
        val elements = this.content()
        val element = elements.getOrNull(id) ?:
                throw Exception("Couldn't find syntax element with id $id inside $this")

        // TODO is this actually good? maybe parameters available by both name and id would be good?
        if (element.name != null)
            throw Exception("Couldn't find syntax element with id $id inside $this")

        return element
    }

    private fun SyntaxElement<*>.findElementByName(name: String): SyntaxElement<*> {
        for (element in iterator()) {
            if (element.name == name)
                return element
        }

        throw Exception("Couldn't find syntax element with name '$name' inside $this")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as MixedParserRegistryImpl

        if (nameToParserMap != other.nameToParserMap) return false
        if (positionToParserMap != other.positionToParserMap) return false
        if (matcher != other.matcher) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nameToParserMap.hashCode()
        result = 31 * result + positionToParserMap.hashCode()
        result = 31 * result + matcher.hashCode()
        return result
    }

    override fun toString(): String {
        return "MixedParserRegistryImpl(nameToParserMap=$nameToParserMap, positionToParserMap=$positionToParserMap, matcher=$matcher)"
    }
}