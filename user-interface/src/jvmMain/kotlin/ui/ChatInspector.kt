@file:Suppress("FunctionName")

package ui

import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.key.ExperimentalKeyInput
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import messaging_api.Author
import messaging_api.IInspectionAPI
import messaging_api.MessageFilter
import messaging_api.impl.DatabaseImpl


@OptIn(ExperimentalKeyInput::class)
fun main() {
    ChatInspector()
}

@ExperimentalKeyInput
fun ChatInspector(location: IntOffset? = null, onClose: (()-> Unit)? = null) {
    Window(
        size = IntSize(640, 1080),
        location = location ?: IntOffset.Zero,
        centered = location == null,
        title = "ChatInspector",
        events = WindowEvents(onClose = onClose)
    ) {
        val api: IInspectionAPI = DatabaseImpl
        onActive {
            onDispose {
                api.close()
            }
        }

        MaterialTheme(
            colors = darkColors
        ) {
            FilterScreen(api)
        }
    }
}

@ExperimentalKeyInput
@Composable
fun FilterScreen(api: IInspectionAPI) {
    val filter = remember { mutableStateOf(MessageFilter(null, null, null)) }
    val filteredMessages = api.filteredMessagesStateFlow.collectAsState(emptyList())

    // Apply Filter when the filter changed (also runs first composition)
    LaunchedEffect(filter.value) {
        api.applyFilter(filter.value)
    }

    val userInConversation = api.authorsStateFlow.collectAsState(emptyList())

    val hasUpdates = api.hasUpdates.collectAsState(false)

    Scaffold(
        topBar = {
            FilterForm(
                usernames = userInConversation.value,
                setFilter = { messageFilter, force ->
                    println("Filter $messageFilter")
                    if (force) {
                        GlobalScope.launch {
                            api.applyFilter(messageFilter)
                        }
                    } else {
                        filter.value = messageFilter
                    }
                },
                hasUpdate = hasUpdates.value
            )
        },
        bodyContent = {
            WithConstraints {
                val density = AmbientDensity.current
                val width = with(density) { constraints.maxWidth.toDp() - it.start - it.end }
                    .takeIf { it.value >= 0 } ?: 0.dp
                val height = with(density) { constraints.maxHeight.toDp() - it.top - it.bottom }
                    .takeIf { it.value >= 0 } ?: 0.dp


                Box(modifier = Modifier.size(width, height)) {
                    MessageList(
                        msgs = filteredMessages.value,
                        clientEMail = density.hashCode().toString(),
                        forceDateTimeDisplay = true
                    )
                }
            }
        },

        )
}


@ExperimentalKeyInput
@Composable
fun FilterForm(
    usernames: List<Author>,
    setFilter: (filter: MessageFilter, force: Boolean) -> Unit,
    hasUpdate: Boolean
) {
    val (startValue, setStartValue) = remember { mutableStateOf("") }
    val (endValue, setEndValue) = remember { mutableStateOf("") }
    val toggleValue = remember { mutableStateOf<Author?>(null) }
    // Auto Update Filter on Change
    val editFilter = remember(toggleValue.value, startValue, endValue) {
        MessageFilter(
            author = toggleValue.value,
            startDateTime = startValue.parseDateTime(),
            endDateTime = endValue.parseDateTime()
        ).also{
            setFilter.invoke(it, false)
        }

    }
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth()
        ) {
            val toggleState = remember { mutableStateOf(false) }
            Text("Filter Messages")
            Row {
                Text("User:")
                DropdownMenu(
                    expanded = toggleState.value,
                    toggle = {
                        OutlinedButton(
                            onClick = {
                                toggleState.value = !toggleState.value
                            }
                        ) {
                            Text(text = toggleValue.value?.toDisplayString() ?: "Select a user")
                            Icon(
                                modifier = Modifier.rotate(if (toggleState.value) 90f else 270f),
                                imageVector = Icons.Filled.ChevronLeft
                            )
                        }
                    },
                    onDismissRequest = {
                        toggleState.value = !toggleState.value
                    },
                    dropdownModifier = Modifier.background(MaterialTheme.colors.background).padding(all = 4.dp),
                    dropdownContent = {
                        MaterialTheme(colors = darkColors) {
                            Surface(
                                color = MaterialTheme.colors.background,
                                elevation = 1.dp
                            ) {
                                Column {
                                    OutlinedButton(onClick = {
                                        toggleValue.value = null
                                        toggleState.value = !toggleState.value
                                    }) {
                                        Text(text = "Clear Selection")
                                    }
                                    for (item in usernames) {
                                        OutlinedButton(onClick = {
                                            toggleValue.value = item
                                            toggleState.value = !toggleState.value
                                        }) {
                                            Text(text = item.toDisplayString())
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
            Column {
                Text("Time Interval")
                val placeholderString = "Enter Date (yyyy.MM.dd HH:mm:ss)"
                Row {
                    Text("Start")
                    EnterText(
                        value = startValue,
                        onValueChange = setStartValue,
                        placeholderString = placeholderString,
                        onApply = null
                    )
                }
                Row {
                    Text("End")
                    EnterText(
                        value = endValue,
                        onValueChange = setEndValue,
                        placeholderString = placeholderString,
                        onApply = null
                    )
                }
            }
            if (hasUpdate) {
                Column {
                    Spacer(
                        modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colors.primary)
                    )
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            setFilter(editFilter.copy(), true)
                        }
                    ) {
                        Text(text = "There are new messages. Press to reapply filter.")
                    }
                }
            }
        }
    }
}
