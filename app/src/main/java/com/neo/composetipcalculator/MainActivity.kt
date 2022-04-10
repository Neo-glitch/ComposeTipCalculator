package com.neo.composetipcalculator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neo.composetipcalculator.components.InputField
import com.neo.composetipcalculator.ui.theme.ComposeTipCalculatorTheme
import com.neo.composetipcalculator.utils.calculateTotalPerson
import com.neo.composetipcalculator.utils.calculateTotalTip
import com.neo.composetipcalculator.widgets.RoundIconButton

class MainActivity : ComponentActivity() {
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                MainContent()
            }
        }
    }
}

// container composable function (take's another composable function)
@Composable
fun MyApp(content: @Composable () -> Unit) {
    ComposeTipCalculatorTheme {
        Surface(color = MaterialTheme.colors.background) {
            content()
        }
    }
}


@Composable
fun TopHeader(totalPerPerson: Double = 133.0) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(150.dp)
//            .clip(shape = RoundedCornerShape(corner = CornerSize(12.dp)))
            .clip(shape = CircleShape.copy(all = CornerSize(12.dp))), // to override corner radius in circle shape
        color = Color(0xFFE9D7F7)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val total = "%.2f".format(totalPerPerson) // formats amount to 2 decimals
            Text(
                text = "Total Per Person",
                style = MaterialTheme.typography.h5
            )
            Text(
                text = "$${total}",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@ExperimentalComposeUiApi
@Preview(heightDp = 300)
@Composable
fun MainContent() {
    val splitByState = remember { mutableStateOf(1) }
    val tipAmountState = remember { mutableStateOf(0.0) }
    val totalPerPersonState = remember { mutableStateOf(0.0) }

    val range = IntRange(start = 1, endInclusive = 100)
    BillForm(
        splitByState = splitByState,
        tipAmountState = tipAmountState,
        totalPerPersonState = totalPerPersonState,
        range = range
    ) { billAmt ->
        Log.d("Main Content", "Amount: $billAmt")

    }

}


@ExperimentalComposeUiApi
@Composable
fun BillForm(
    modifier: Modifier = Modifier,
    range: IntRange = 1..100,
    splitByState: MutableState<Int>,
    tipAmountState: MutableState<Double>,
    totalPerPersonState: MutableState<Double>,
    onValChange: (String) -> Unit = {}
) {
    val totalBillState = remember { mutableStateOf("") }

    // state to check if text is entered and is valid
    // value to monitor is totalBillState.value
    val validState = remember(totalBillState.value) {
        totalBillState.value.trim().isNotEmpty()
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    var sliderPositionState by remember { mutableStateOf(0f) }
    val tipPercentage = (sliderPositionState * 100).toInt()



    Column {
        TopHeader(totalPerPersonState.value)

        Surface(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(CornerSize(8.dp)),
            border = BorderStroke(width = 1.dp, color = Color.LightGray)
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {

                InputField(
                    valueState = totalBillState,
                    labelId = "Enter Bill",
                    enabled = true,
                    isSingleLine = true,
                    onAction = KeyboardActions {
                        // once enter key is pressed, to text isn't empty
                        if (!validState) return@KeyboardActions

                        onValChange(totalBillState.value.trim())

                        // validState is true so hide the keyboard after Enter is pressed
                        keyboardController?.hide()
                    })
                if (validState) {
                    totalPerPersonState.value =
                        calculateTotalPerson(
                            totalBill = totalBillState.value.toDouble(),
                            splitBy = splitByState.value, tipPercentage = tipPercentage
                        )
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Split",
                            modifier = Modifier.align(
                                alignment = Alignment.CenterVertically
                            )
                        )
                        Spacer(modifier = Modifier.width(120.dp))
                        Row(
                            modifier = Modifier.padding(horizontal = 3.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            RoundIconButton(imageVector = Icons.Default.Remove,
                                onClick = { if (splitByState.value > range.first) {
                                    splitByState.value--
                                    totalPerPersonState.value =
                                        calculateTotalPerson(
                                            totalBill = totalBillState.value.toDouble(),
                                            splitBy = splitByState.value, tipPercentage = tipPercentage
                                        )
                                }})
                            Text(
                                text = splitByState.value.toString(),
                                modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 9.dp, end = 9.dp)
                            )
                            RoundIconButton(imageVector = Icons.Default.Add,
                                onClick = { if (splitByState.value < range.last) {
                                    splitByState.value++
                                    totalPerPersonState.value =
                                        calculateTotalPerson(
                                            totalBill = totalBillState.value.toDouble(),
                                            splitBy = splitByState.value, tipPercentage = tipPercentage
                                        )
                                }})
                        }
                    }

                    // Tip Row
                    Row(modifier = Modifier.padding(horizontal = 3.dp, vertical = 12.dp)) {
                        Text(
                            text = "Tip",
                            modifier = Modifier.align(alignment = Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(200.dp))
                        Text(
                            text = "$ ${tipAmountState.value}",
                            modifier = Modifier.align(alignment = Alignment.CenterVertically)
                        )
                    }

                    // percentage slider component
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "${tipPercentage}%")
                        Spacer(modifier = Modifier.height(14.dp))
                        Slider(value = sliderPositionState,
                            onValueChange = { newValue ->
                                sliderPositionState = newValue
                                tipAmountState.value =
                                    calculateTotalTip(
                                        totalBill = totalBillState.value.toDouble(),
                                        tipPercentage = tipPercentage
                                    )
                                totalPerPersonState.value =
                                    calculateTotalPerson(
                                        totalBill = totalBillState.value.toDouble(),
                                        splitBy = splitByState.value, tipPercentage = tipPercentage
                                    )
                            },
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                            steps = 5, onValueChangeFinished = {})
                    }
                } else {
                    Box {

                    }
                }
            }
        }
    }


}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp {
        Text("Hello again")
    }

}