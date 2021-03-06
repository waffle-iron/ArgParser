package com.github.mibac138.argparser

import com.github.mibac138.argparser.exception.ParserInvalidInputException
import com.github.mibac138.argparser.parser.BooleanParser
import com.github.mibac138.argparser.reader.asReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by mibac138 on 06-04-2017.
 */
class BooleanParserTest : ParserTest() {
    private val parser = BooleanParser()

    @Test fun supportedTypes() {
        assertTrue(parser.supportedTypes == setOf(Boolean::class.java, Boolean::class.javaObjectType))
    }

    @Test fun parse() {
        assertEquals(true, parser.parse("yes".asReader(), reqElement()))
        assertEquals(true, parser.parse("true".asReader(), reqElement()))

        assertEquals(false, parser.parse("no".asReader(), reqElement()))
        assertEquals(false, parser.parse("false".asReader(), reqElement()))
    }

    @Test(expected = ParserInvalidInputException::class) fun invalid() {
        parser.parse("maybe".asReader(), reqElement())
    }
}