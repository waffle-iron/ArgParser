package com.github.mibac138.argparser.parser

import com.github.mibac138.argparser.named.name
import com.github.mibac138.argparser.reader.asReader
import com.github.mibac138.argparser.syntax.defaultValue
import com.github.mibac138.argparser.syntax.dsl.element
import com.github.mibac138.argparser.syntax.dsl.syntaxContainer
import com.github.mibac138.argparser.syntax.dsl.syntaxElement
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by mibac138 on 07-05-2017.
 */
class MixedParserRegistryImplTest {
    val parser = MixedParserRegistryImpl()

    @Test
    fun basicTest() {
        parser.registerParser(SequenceParser(), 0)
        parser.registerParser(SequenceParser(), 1)
        val output = parser.parse("Hello! :)".asReader(), syntaxContainer {
            element(String::class.java)
            element(String::class.java)
        })

        assertEquals(
                listOf("Hello!", ":)"),
                output[null])
    }

    @Test
    fun mediumTest() {
        parser.registerParser(IntParser(), 0)
        parser.registerParser(SequenceParser(), "name")
        val syntax = syntaxContainer {
            element(Int::class.java)
            element(String::class.java, { name = "name" })
        }
        val output = parser.parse("15 --name:Mike".asReader(), syntax)

        assertEquals(
                mapOf(
                        null to listOf(15), "name" to "Mike"
                     ),
                output.keyToValueMap)
    }

    @Test
    fun complexTest() {
        parser.registerParser(BooleanParser(), 0)
        parser.registerParser(IntParser(), 1)
        parser.registerParser(BooleanParser(), 2)
        parser.registerParser(SequenceParser(), "seq1")
        parser.registerParser(SequenceParser(), "seq2")
        parser.registerParser(SequenceParser(), "seq3")
        val syntax = syntaxContainer {
            element(Boolean::class.java)
            element(Int::class.java)
            element(Boolean::class.java)
            element(String::class.java, { name = "seq1" })
            element(String::class.java, { name = "seq2" })
            element(String::class.java, { name = "seq3" })
        }
        val output = parser.parse("true --seq1='Hiya!' --seq2: 'Lorem ipsum' 100 false --seq3=Hi".asReader(), syntax)

        assertEquals(
                mapOf(
                        null to listOf(true, 100, false),
                        "seq1" to "'Hiya!'",
                        "seq2" to "'Lorem ipsum'",
                        "seq3" to "Hi"
                     ), output.keyToValueMap)
    }


    @Test
    fun issue10() {
        parser.registerParser(SequenceParser(), 0)
        parser.registerParser(SequenceParser(), 1)
        parser.registerParser(SequenceParser(), "seq")
        val syntax = syntaxContainer {
            element(String::class.java) { name = "seq" }
            element(String::class.java)
            element(String::class.java) { required = false; defaultValue = "default" }
        }

        val output = parser.parse("yes --seq:sequence".asReader(), syntax)

        assertEquals(
                mapOf(
                        null to listOf("yes", "default"),
                        "seq" to "sequence"
                     ),
                output.keyToValueMap)
    }

    @Test
    fun issue9() {
        parser.registerParser(SequenceParser(), 0)

        val syntax = syntaxElement(String::class.java) {
            required = false
            defaultValue = "default"
        }

        val output = parser.parse(" ".asReader(), syntax)
        assertEquals<Map<String?, Any?>>(
                mapOf(
                        null to listOf("default")
                     ),
                output.keyToValueMap)
    }
}