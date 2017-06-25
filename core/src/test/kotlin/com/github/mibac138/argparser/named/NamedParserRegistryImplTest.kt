package com.github.mibac138.argparser.named

import com.github.mibac138.argparser.exception.ParserException
import com.github.mibac138.argparser.exception.ParserInvalidInputException
import com.github.mibac138.argparser.parser.BooleanParser
import com.github.mibac138.argparser.parser.Parser
import com.github.mibac138.argparser.reader.ArgumentReader
import com.github.mibac138.argparser.reader.asReader
import com.github.mibac138.argparser.syntax.SyntaxElement
import com.github.mibac138.argparser.syntax.SyntaxElementImpl
import com.github.mibac138.argparser.syntax.dsl.syntaxElement
import com.github.mibac138.argparser.syntax.parser
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Created by mibac138 on 03-05-2017.
 */
class NamedParserRegistryImplTest {
    private val parser = NamedParserRegistryImpl()

    @Test fun test() {
        parser.registerParser(BooleanParser())
        parser.associateParserWithName(Boolean::class.java, "hi")
        val result = parser.parse("--hi: true".asReader(), SyntaxElementImpl(Boolean::class.java, null, NameComponent("hi")))
        assertEquals(mapOf("hi" to true), result)
    }

    @Test fun testSupportedTypes() {
        assertEquals(emptySet(), parser.getSupportedTypes())

        parser.registerParser(BooleanParser())

        assertEquals(setOf(Boolean::class.java, Boolean::class.javaObjectType), parser.getSupportedTypes())

        parser.removeParser(BooleanParser())

        assertEquals(emptySet(), parser.getSupportedTypes())
    }

    @Test fun testCustomParser() {
        val output = parser.parse("--hi: true".asReader(), syntaxElement(Boolean::class.java, {
            name = "hi"
            parser = InvertedBooleanParser(BooleanParser())
        }))

        assertEquals(mapOf("hi" to false), output)
    }

    @Test(expected = IllegalArgumentException::class) fun testRemoveParserWithAssociatedNames() {
        parser.registerParser(BooleanParser(), "bool")
        parser.associateParserWithName(Boolean::class.java, "value")
        parser.associateParserWithName(Boolean::class.java, "boolean")

        parser.removeParser(BooleanParser())

        // Expected to throw here. (couldn't find parser for name value)
        parser.parse("--value: true".asReader(), SyntaxElementImpl(Boolean::class.java, components = NameComponent("value")))
    }

    @Test(expected = IllegalArgumentException::class) fun testAssocParserWithNameInvalid() {
        parser.associateParserWithName(Any::class.java, "object")
    }

    @Test(expected = IllegalArgumentException::class) fun testUnknownType() {
        println(parser.parse("--hi: Hello!".asReader(), SyntaxElementImpl(String::class.java, components = NameComponent("hi"))))
    }

    @Test(expected = IllegalArgumentException::class) fun testInvalidSyntax() {
        parser.parse("".asReader(), SyntaxElementImpl(Any::class.java))
    }

    @Test(expected = IllegalArgumentException::class) fun testInvalidInput() {
        parser.parse("--".asReader(), SyntaxElementImpl(Any::class.java, NameComponent("")))
    }

    @Test fun testProblematicParser1() {
        parser.registerParser(object : Parser {
            override fun getSupportedTypes(): Set<Class<*>> = setOf(Any::class.java)

            override fun parse(input: ArgumentReader, syntax: SyntaxElement<*>): Any
                    = throw Exception()
        }, "color")

        val result = parser.parse("--color:".asReader(), SyntaxElementImpl(Any::class.java, components = NameComponent("color")))
        assertTrue(result["color"] is ParserException)
    }


    @Test fun testProblematicParser2() {
        parser.registerParser(object : Parser {
            override fun getSupportedTypes(): Set<Class<*>> = setOf(Any::class.java)

            override fun parse(input: ArgumentReader, syntax: SyntaxElement<*>): Any
                    = throw ParserInvalidInputException()
        }, "color")

        val result = parser.parse("--color:".asReader(), SyntaxElementImpl(Any::class.java, components = NameComponent("color")))
        assertTrue(result["color"] is ParserException)
    }

    @Test fun testEquality() {
        val a = NamedParserRegistryImpl()
        val b = NamedParserRegistryImpl()

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(a.toString(), b.toString())

        a.registerParser(BooleanParser(), "bool")
        b.registerParser(BooleanParser(), "bool")

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(a.toString(), b.toString())
    }

    class InvertedBooleanParser(private val parser: BooleanParser) : Parser by parser {
        override fun parse(input: ArgumentReader, syntax: SyntaxElement<*>): Any = !parser.parse(input, syntax)
    }
}