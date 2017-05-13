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

import com.github.mibac138.argparser.exception.ParserException
import com.github.mibac138.argparser.named.ArgumentMatcher
import com.github.mibac138.argparser.named.DefaultArgumentMatcher
import com.github.mibac138.argparser.named.name
import com.github.mibac138.argparser.reader.ArgumentReader
import com.github.mibac138.argparser.reader.skipChar
import com.github.mibac138.argparser.syntax.SyntaxElement
import com.github.mibac138.argparser.syntax.getRequiredSize
import com.github.mibac138.argparser.syntax.iterator
import com.github.mibac138.argparser.syntax.parser
import java.util.regex.Pattern

/**
 * Can parse mixed syntax (i.e. with both named and unnamed elements)
 *
 * *Note*: Currently unnamed not required element's *must* be at the end of the syntax
 */
class MixedParserRegistryImpl : MixedParserRegistry {
    private val nameToParserMap: MutableMap<String, Parser> = HashMap()
    private val positionToParserMap: MutableMap<Int, Parser> = HashMap()
    override var matcher: ArgumentMatcher = DefaultArgumentMatcher(Pattern.compile("--([a-zA-Z_0-9]+)(?:=|: ?)"))

    override fun getSupportedTypes(): Set<Class<*>> {
        val output = mutableSetOf<Class<*>>()
        nameToParserMap.values.map { it.getSupportedTypes() }.forEach { output += it }
        positionToParserMap.values.map { it.getSupportedTypes() }.forEach { output += it }

        return output
    }

    override fun parse(input: ArgumentReader, syntax: SyntaxElement<*>): Map<String?, *> {
        val named = HashMap<String?, Any?>()
        val unnamed = ArrayList<Any?>()

        var index = 0
        for (i in 0 until syntax.getRequiredSize()) {
            input.skipChar(' ')
            val matched = matcher.match(input)

            if (matched == null) {
                positionToParserMap[index]?.let {
                    unnamed += parseElement(input, syntax.findElementById(index), it)
                }
                index++
            } else {
                val element = syntax.findElementByName(matched.name)
                val parser = getParserForElement(element)
                val parsed = parseElement(matched.value, element, parser)
                named[matched.name] = parsed
            }
        }

        named[null] = unnamed
        return named
    }

    private fun parseElement(input: ArgumentReader, element: SyntaxElement<*>, parser: Parser): Any {
        input.skipChar(' ')
        input.mark()
        var parsed: Any
        try {
            parsed = parser.parse(input, element)
        } catch (e: Exception) {
            parsed = e.wrap()
        }

        input.removeMark()
        return parsed
    }

    private fun Exception.wrap(): Exception {
        if (this is ParserException)
            return this
        return ParserException(this)
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

    override fun removeParser(parser: Parser) {
        throw UnsupportedOperationException()
    }

    override fun removeAllParsers() {
        nameToParserMap.clear()
        positionToParserMap.clear()
    }

    private fun getParserForElement(element: SyntaxElement<*>): Parser {
        return element.parser ?: getParserForName(element.name ?:
                throw IllegalArgumentException())
    }

    private fun getParserForName(name: String): Parser {
        val parser = nameToParserMap[name]
        if (parser != null)
            return parser

        throw IllegalArgumentException("Couldn't find parser for name '$name'")
    }

    private fun SyntaxElement<*>.findElementById(id: Int): SyntaxElement<*> {
        var index = 0
        for (element in iterator()) {
            if (element.name != null)
                continue

            if (index++ == id)
                return element
        }

        throw Exception("Couldn't find syntax element with id $id inside $this")
    }

    private fun SyntaxElement<*>.findElementByName(name: String): SyntaxElement<*> {
        for (element in iterator()) {
            if (element.name == name)
                return element
        }

        throw Exception("Couldn't find syntax element with name '$name' inside $this")
    }
}