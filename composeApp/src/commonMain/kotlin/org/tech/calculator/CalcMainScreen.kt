package org.tech.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow

@Composable
fun CalcMainScreen() {

    val arrayOfElement =
        arrayOf("00", 0, ".", "=", 1, 2, 3, "+", 4, 5, 6, "-", 7, 8, 9, "*", "C", "%", "x", "/")
    var inputString by remember {
        mutableStateOf("")
    }
    var isEqual by remember {
        mutableStateOf(false)
    }
    val specialChar = arrayOf("+", "-", "*", "/", "%")
    Box(
        modifier = Modifier.fillMaxSize().background(Color.LightGray)
    ) {
        Column {
            Box(modifier = Modifier.weight(1f).fillMaxSize().background(Color.LightGray)) {
                TextField(
                    value = inputString,
                    onValueChange = {
                        inputString = it
                    },
                    readOnly = true,
                    modifier = Modifier.align(Alignment.BottomEnd).fillMaxWidth(),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 30.sp
                    ),
                    colors = TextFieldDefaults.textFieldColors(
                        disabledTextColor = Color.LightGray,
                        backgroundColor = Color.LightGray,
                        focusedLabelColor = Color.LightGray,
                        unfocusedLabelColor = Color.LightGray,
                        disabledIndicatorColor = Color.LightGray,
                        unfocusedIndicatorColor = Color.LightGray,
                        focusedIndicatorColor = Color.LightGray
                    )
                )
            }
            Box(modifier = Modifier.weight(1.5f).fillMaxSize().background(Color.LightGray)) {
                LazyVerticalStaggeredGrid(
                    verticalItemSpacing = 10.dp,
                    reverseLayout = true,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    columns = StaggeredGridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize().background(Color.LightGray),
                    contentPadding = PaddingValues(10.dp)
                ) {
                    items(arrayOfElement) {
                        ButtonComponent(
                            iconString = it.toString(),
                            onClick = {
                                when (it) {
                                    "C" -> {
                                        inputString = ""
                                    }

                                    "=" -> {
                                        inputString = calculateString(inputString).toString()
                                        isEqual = true
                                    }

                                    "x" -> {
                                        if (isEqual) {
                                            inputString = ""
                                            isEqual = false
                                        } else {
                                            inputString = inputString.dropLast(1)
                                        }
                                    }

                                    else -> {
                                        if (isEqual) {
                                            inputString = ""
                                            inputString += it.toString()
                                            isEqual = false
                                        } else {
                                            if (inputString.isEmpty() && !specialChar.contains(it.toString())) {
                                                inputString += it.toString()
                                            } else if (inputString.isNotEmpty() && ((inputString.last()
                                                    .toString() == "+" && it.toString() == "+") || (inputString.last()
                                                    .toString() == "-" && it.toString() == "-") || (inputString.last()
                                                    .toString() == "*" && it.toString() == "*") || (inputString.last()
                                                    .toString() == "/" && it.toString() == "/") || (inputString.last()
                                                    .toString() == "%" && it.toString() == "%")) || (specialChar.contains(
                                                    it.toString()
                                                ) && (inputString.isNotEmpty() && specialChar.contains(
                                                    inputString[inputString.length - 1].toString()
                                                )))
                                            ) {
                                                inputString = inputString.dropLast(1)
                                                inputString += it.toString()
                                            } else {
                                                if (inputString.isNotEmpty()) {
                                                    inputString += it.toString()
                                                }
                                            }
                                            isEqual = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

fun calculateString(input: String): Double {
    val tokens = tokenize(input)
    println(tokens)
    val rpn = toRPN(tokens)
    println(rpn)

    for ((index, element) in rpn.withIndex()) {
        if (index < rpn.size - 1) {
            if ((rpn[index] == "+" && rpn[index + 1] == "+") || (rpn[index] == "*" && rpn[index + 1] == "*") || (rpn[index] == "/" && rpn[index + 1] == "/") || (rpn[index] == "-" && rpn[index + 1] == "-") || (rpn[index] == "%" && rpn[index + 1] == "%")) {
                rpn.removeAt(index + 1)
            }
        }
    }
    println(rpn)
    return evaluateRPN(rpn)
}

private fun tokenize(input: String): MutableList<String> {
    val regex = """(\d+\.\d+|\d+|[+\-*/^%()])""".toRegex()
    return regex.findAll(input).map { it.value }.filter { it.isNotBlank() }.toMutableList()
}

private fun toRPN(tokens: List<String>): MutableList<String> {
    val output = mutableListOf<String>()
    val operators = ArrayDeque<String>()

    for (token in tokens) {
        when {
            token.isDouble() -> output.add(token)
            token == "(" -> operators.addLast(token)
            token == ")" -> {
                while (operators.isNotEmpty() && operators.last() != "(") {
                    output.add(operators.removeLast())
                }
                operators.removeLast() // Remove '('
            }

            else -> {
                while (operators.isNotEmpty() && operators.last()
                        .precedence() >= token.precedence()
                ) {
                    output.add(operators.removeLast())
                }
                operators.addLast(token)
            }
        }
    }
    while (operators.isNotEmpty()) {
        output.add(operators.removeLast())
    }
    return output
}

private fun evaluateRPN(tokens: List<String>): Double {
    val stack = ArrayDeque<Double>()

    for (token in tokens) {
        if (token.isDouble()) {
            stack.addLast(token.toDouble())
        } else {
            val b = stack.removeLast()
            val a =
                if (token == "%") 0.0 else stack.removeLast() // For percentage, only one operand is needed
            stack.addLast(
                when (token) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> a / b
                    "^" -> a.pow(b)
                    "%" -> a * b / 100 // Treat % as division by 100
                    else -> throw IllegalArgumentException("Unknown operator: $token")
                }
            )
        }
    }
    return stack.last()
}

private fun String.isDouble() = this.toDoubleOrNull() != null

private fun String.precedence(): Int {
    return when (this) {
        "+", "-" -> 1
        "*", "/" -> 2
        "%", "^" -> 3
        else -> 0
    }
}

@Composable
fun ButtonComponent(
    iconString: String,
    onClick: () -> Unit
) {
    val color =
        if (iconString == "C" || iconString == "+" || iconString == "/" || iconString == "%" || iconString == "*" || iconString == "x" || iconString == "-") Color.Gray else if (iconString == "=") Color.Magenta else Color.White
    Box(
        modifier = Modifier.size(70.dp).shadow(
            shape = CircleShape,
            elevation = 5.dp
        ).background(
            color = color,
            shape = CircleShape
        ).clickable {
            onClick()
        },
    ) {
        Text(
            text = iconString,
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                color = Color.Black,
                fontSize = 30.sp
            )
        )
    }
}
