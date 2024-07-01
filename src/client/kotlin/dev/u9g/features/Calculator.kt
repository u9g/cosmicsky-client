package dev.u9g.features

import com.mojang.brigadier.arguments.StringArgumentType
import dev.u9g.commands.get
import dev.u9g.commands.thenArgument
import dev.u9g.commands.thenExecute
import dev.u9g.events.CommandCallback
import net.minecraft.text.Text
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

const val calculationPrecision = 5

class Calculator {
    init {
        CommandCallback.event.register {
            it.register("calc") {
                thenArgument("what to calculate", StringArgumentType.string()) {
                    thenExecute {
                        if (Settings.enableMod) {
                            val calculation = this[it]
                            val formatted = NEUCalculator.decimalFormat.format(NEUCalculator.calculate(calculation))
                            source.sendFeedback(Text.literal("§f$calculation §e= §a$formatted"))
                        }
                    }
                }
            }
        }
    }
}

object NEUCalculator {
    val decimalFormat: DecimalFormat
        get() {
            val f = StringBuilder("#,##0.")
            for (i in 0 until calculationPrecision) {
                f.append("#")
            }
            return DecimalFormat(f.toString())
        }

    @JvmOverloads
    @Throws(CalculatorException::class)
    fun calculate(
        source: String,
        variables: VariableProvider = object : VariableProvider {
            override fun provideVariable(name: String?) = Optional.empty<BigDecimal>()
        }
    ): BigDecimal {
        return evaluate(variables, shuntingYard(lex(source)))
    }

    var binops: String = "+-*/^x"
    var postops: String = "mkbts%"
    var digits: String = "0123456789"
    var nameCharacters: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ_"

    fun readDigitsInto(token: NEUCalculator.Token, source: String, decimals: Boolean) {
        val startIndex: Int = token.tokenStart + token.tokenLength
        var j = 0
        while (j + startIndex < source.length) {
            val d = source[j + startIndex]
            val d0: Int = NEUCalculator.digits.indexOf(d)
            if (d0 != -1) {
                if (decimals) token.exponent--
                token.numericValue *= 10
                token.numericValue += d0.toLong()
                token.tokenLength += 1
            } else {
                return
            }
            j++
        }
    }

    @Throws(CalculatorException::class)
    fun lex(source: String): List<NEUCalculator.Token> {
        val tokens: MutableList<NEUCalculator.Token> = ArrayList<NEUCalculator.Token>()
        var doesNotHaveLValue = true
        var i = 0
        while (i < source.length) {
            val c = source[i]
            if (Character.isWhitespace(c)) {
                i++
                continue
            }
            val token: NEUCalculator.Token = NEUCalculator.Token()
            token.tokenStart = i
            if (doesNotHaveLValue && c == '-') {
                token.tokenLength = 1
                token.type = TokenType.PREOP
                token.operatorValue = "-"
            } else if (NEUCalculator.binops.indexOf(c) != -1) {
                token.tokenLength = 1
                token.type = TokenType.BINOP
                token.operatorValue = c.toString()
                if (c == '*' && i + 1 < source.length && source[i + 1] == '*') {
                    token.tokenLength++
                    token.operatorValue = "^"
                }
            } else if (NEUCalculator.postops.indexOf(c) != -1) {
                token.tokenLength = 1
                token.type = TokenType.POSTOP
                token.operatorValue = c.toString().lowercase()
            } else if (c == ')') {
                token.tokenLength = 1
                token.type = TokenType.RPAREN
                token.operatorValue = ")"
            } else if (c == '(') {
                token.tokenLength = 1
                token.type = TokenType.LPAREN
                token.operatorValue = "("
            } else if ('.' == c || ',' == c) {
                token.tokenLength = 1
                token.type = TokenType.NUMBER
                NEUCalculator.readDigitsInto(token, source, true)
                if (token.tokenLength == 1) {
                    throw CalculatorException("Invalid number literal", i, 1)
                }
            } else if ('$' == c) {
                token.tokenLength = 1
                token.type = TokenType.VARIABLE
                token.operatorValue = ""
                var inParenthesis = false
                if (i + 1 < source.length && source[i + 1] == '{') {
                    token.tokenLength++
                    inParenthesis = true
                }
                for (j in token.tokenStart + token.tokenLength until source.length) {
                    val d = source[j]
                    if (inParenthesis) {
                        if (d == '}') {
                            token.tokenLength++
                            inParenthesis = false
                            break
                        }
                    } else if (NEUCalculator.nameCharacters.indexOf(d) == -1) break
                    token.operatorValue += d
                    token.tokenLength++
                }
                if (token.operatorValue!!.isEmpty() || inParenthesis) {
                    throw CalculatorException("Unterminated variable literal", token.tokenStart, token.tokenLength)
                }
            } else if (NEUCalculator.digits.indexOf(c) != -1) {
                token.type = TokenType.NUMBER
                NEUCalculator.readDigitsInto(token, source, false)
                if (i + token.tokenLength < source.length) {
                    val p = source[i + token.tokenLength]
                    if ('.' == p || ',' == p) {
                        token.tokenLength++
                        NEUCalculator.readDigitsInto(token, source, true)
                    }
                }
            } else {
                throw CalculatorException("Unknown thing $c", i, 1)
            }
            doesNotHaveLValue =
                token.type == TokenType.LPAREN || token.type == TokenType.PREOP || token.type == TokenType.BINOP
            tokens.add(token)
            i += token.tokenLength
        }
        return tokens
    }

    ///</editor-fold>
    ///<editor-fold desc="Shunting Time">
    @Throws(CalculatorException::class)
    fun getPrecedence(token: NEUCalculator.Token): Int {
        when (token.operatorValue!!.intern()) {
            "+", "-" -> return 0
            "*", "/", "x" -> return 1
            "^" -> return 2
        }
        throw CalculatorException("Unknown operator " + token.operatorValue, token.tokenStart, token.tokenLength)
    }

    @Throws(CalculatorException::class)
    fun shuntingYard(toShunt: List<NEUCalculator.Token?>): List<NEUCalculator.Token> {
        // IT'S SHUNTING TIME
        // This is an implementation of the shunting yard algorithm

        val op: Deque<NEUCalculator.Token> = ArrayDeque<NEUCalculator.Token>()
        val out: MutableList<NEUCalculator.Token> = ArrayList<NEUCalculator.Token>()

        for (currentlyShunting in toShunt) {
            when (currentlyShunting!!.type!!) {
                TokenType.NUMBER, TokenType.VARIABLE -> out.add(currentlyShunting)
                TokenType.BINOP -> {
                    val p: Int = NEUCalculator.getPrecedence(currentlyShunting)
                    while (!op.isEmpty()) {
                        val l: NEUCalculator.Token = op.peek()
                        if (l.type == TokenType.LPAREN) break
                        assert(l.type == TokenType.BINOP || l.type == TokenType.PREOP)
                        val pl: Int = NEUCalculator.getPrecedence(l)
                        if (pl >= p) { // Association order
                            out.add(op.pop())
                        } else {
                            break
                        }
                    }
                    op.push(currentlyShunting)
                }

                TokenType.PREOP -> op.push(currentlyShunting)
                TokenType.LPAREN -> op.push(currentlyShunting)
                TokenType.RPAREN -> while (true) {
                    if (op.isEmpty()) throw CalculatorException(
                        "Unbalanced right parenthesis",
                        currentlyShunting.tokenStart,
                        currentlyShunting.tokenLength
                    )
                    val l: NEUCalculator.Token = op.pop()
                    if (l.type == TokenType.LPAREN) {
                        break
                    }
                    out.add(l)
                }

                TokenType.POSTOP -> out.add(currentlyShunting)
            }
        }
        while (!op.isEmpty()) {
            val l: NEUCalculator.Token = op.pop()
            if (l.type == TokenType.LPAREN) throw CalculatorException(
                "Unbalanced left parenthesis",
                l.tokenStart,
                l.tokenLength
            )
            out.add(l)
        }
        return out
    }

    /// </editor-fold>
    ///<editor-fold desc="Evaluating Time">
    @Throws(CalculatorException::class)
    fun evaluate(provider: VariableProvider, rpnTokens: List<NEUCalculator.Token>): BigDecimal {
        val values: Deque<BigDecimal> = ArrayDeque()
        val precision =
            calculationPrecision
        try {
            for (command in rpnTokens) {
                when (command.type!!) {
                    TokenType.VARIABLE -> values.push(provider.provideVariable(command.operatorValue)
                        .orElseThrow {
                            CalculatorException(
                                "Unknown variable " + command.operatorValue,
                                command.tokenStart,
                                command.tokenLength
                            )
                        })

                    TokenType.PREOP -> values.push(values.pop().negate())
                    TokenType.NUMBER -> values.push(BigDecimal(command.numericValue).scaleByPowerOfTen(command.exponent))
                    TokenType.BINOP -> {
                        val right = values.pop().setScale(precision, RoundingMode.HALF_UP)
                        val left = values.pop().setScale(precision, RoundingMode.HALF_UP)
                        when (command.operatorValue!!.intern()) {
                            "^" -> {
                                if (right >= BigDecimal(1000)) {
                                    val rightToken: NEUCalculator.Token = rpnTokens[rpnTokens.indexOf(command) - 1]
                                    throw CalculatorException(
                                        "$right is too large, pick a power less than 1000",
                                        rightToken.tokenStart,
                                        rightToken.tokenLength
                                    )
                                }

                                if (right.toDouble() != right.toInt().toDouble()) {
                                    val rightToken: NEUCalculator.Token = rpnTokens[rpnTokens.indexOf(command) - 1]
                                    throw CalculatorException(
                                        "$right has a decimal, pick a power that is non-decimal",
                                        rightToken.tokenStart,
                                        rightToken.tokenLength
                                    )
                                }

                                if (right.toDouble() < 0) {
                                    val rightToken: NEUCalculator.Token = rpnTokens[rpnTokens.indexOf(command) - 1]
                                    throw CalculatorException(
                                        "$right is a negative number, pick a power that is positive",
                                        rightToken.tokenStart,
                                        rightToken.tokenLength
                                    )
                                }
                                values.push(left.pow(right.toInt()).setScale(precision, RoundingMode.HALF_UP))
                            }

                            "x", "*" -> values.push(left.multiply(right).setScale(precision, RoundingMode.HALF_UP))
                            "/" -> try {
                                values.push(
                                    left.divide(right, RoundingMode.HALF_UP).setScale(precision, RoundingMode.HALF_UP)
                                )
                            } catch (e: ArithmeticException) {
                                throw CalculatorException(
                                    "Encountered division by 0",
                                    command.tokenStart,
                                    command.tokenLength
                                )
                            }

                            "+" -> values.push(left.add(right).setScale(precision, RoundingMode.HALF_UP))
                            "-" -> values.push(left.subtract(right).setScale(precision, RoundingMode.HALF_UP))
                            else -> throw CalculatorException(
                                "Unknown operation " + command.operatorValue,
                                command.tokenStart,
                                command.tokenLength
                            )
                        }
                    }

                    TokenType.LPAREN, TokenType.RPAREN -> throw CalculatorException(
                        "Did not expect unshunted token in RPN",
                        command.tokenStart,
                        command.tokenLength
                    )

                    TokenType.POSTOP -> {
                        val p = values.pop()
                        when (command.operatorValue!!.intern()) {
                            "s" -> values.push(p.multiply(BigDecimal(64)).setScale(precision, RoundingMode.HALF_UP))
                            "k" -> values.push(p.multiply(BigDecimal(1000)).setScale(precision, RoundingMode.HALF_UP))
                            "m" -> values.push(
                                p.multiply(BigDecimal(1000000)).setScale(precision, RoundingMode.HALF_UP)
                            )

                            "b" -> values.push(
                                p.multiply(BigDecimal(1000000000)).setScale(precision, RoundingMode.HALF_UP)
                            )

                            "t" -> values.push(
                                p.multiply(BigDecimal("1000000000000")).setScale(precision, RoundingMode.HALF_UP)
                            )

                            "%" -> values.push(
                                p
                                    .setScale(precision + 1, RoundingMode.HALF_UP)
                                    .divide(BigDecimal(100), RoundingMode.HALF_UP)
                                    .setScale(precision, RoundingMode.HALF_UP)
                            )

                            else -> throw CalculatorException(
                                "Unknown operation " + command.operatorValue,
                                command.tokenStart,
                                command.tokenLength
                            )
                        }
                    }
                }
            }
            val peek = values.pop()
            return peek.stripTrailingZeros()
        } catch (e: NoSuchElementException) {
            throw CalculatorException("Unfinished expression", 0, 0)
        }
    } /// </editor-fold>

    interface VariableProvider {
        @Throws(CalculatorException::class)
        fun provideVariable(name: String?): Optional<BigDecimal>
    }

    ///<editor-fold desc="Lexing Time">
    enum class TokenType {
        NUMBER, BINOP, LPAREN, RPAREN, POSTOP, PREOP, VARIABLE
    }

    class Token {
        var type: TokenType? = null
        var operatorValue: String? = null
        var numericValue: Long = 0
        var exponent: Int = 0
        var tokenStart: Int = 0
        var tokenLength: Int = 0
    }

    class CalculatorException(message: String?, var offset: Int, var length: Int) : Exception(message)
}