@file:Suppress("FunctionName")

package ui

import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.AppWindowAmbient
import androidx.compose.desktop.Window
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRowFor
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalKeyInput::class)
fun main(args: Array<String>) {
    Window(
        size = IntSize(640, 1080),
    ) {
        val (clientName, setClientName) = remember { mutableStateOf("") }

        val appWindow: AppWindow? = AppWindowAmbient.current
        remember(clientName) { // Updates the title of the window when the user changes
            val name = clientName.takeUnless { it.isEmpty() } ?: "<None Selected>"
            appWindow?.setTitle("Client: $name")
        }

        val messages: SnapshotStateList<Message> = remember {
            mutableStateListOf(
                Message("Hello, my name is Holger", LocalDateTime.now(), "Holger"),
                Message("I'm Hans", LocalDateTime.now(), "Hans"),
                Message("Nice to meet you all, call me Hugo", LocalDateTime.now(), "Hugo"),
                Message("Does everyone have the agenda for today?", LocalDateTime.now(), "Holger"),
                Message("If not please message me", LocalDateTime.now(), "Holger"),
                Message("Hey.\n Alfons here. I would need a copy.", LocalDateTime.now(), "Alfons"),
            )
        }

        val userInConversation = remember(messages.firstStateRecord) {
            val set = mutableSetOf<String>()
            set.addAll(messages.map { it.author })
            mutableStateListOf(*set.toTypedArray())
        }

        MaterialTheme(
            colors = darkColors(
                background = Color(0xFF202A41),
                primary = Color(0xFFFF4088),
                surface = Color(0xFF1B2439),
            )
        ) {
            Scaffold(
                topBar = {
                    ClientTopBar(
                        clientName = clientName,
                        setClientName = setClientName,
                        usernames = userInConversation
                    )
                },
                bodyContent = {
                    WithConstraints {
                        val density = AmbientDensity.current
                        val width = with(density) { constraints.maxWidth.toDp() - it.start - it.end }
                        val height = with(density) { constraints.maxHeight.toDp() - it.top - it.bottom }

                        Box(modifier = Modifier.size(width, height)) {
                            MessageList(
                                msgs = messages,
                                clientName = clientName,
                                forceDateTimeDisplay = false
                            )
                        }
                    }
                },
                bottomBar = {
                    ComposeMessageBar(
                        onSendClicked = {
                            if (it.isNotEmpty()) {
                                val newMsg = Message(
                                    content = it, timestamp = LocalDateTime.now(), author = clientName
                                )
                                messages.add(newMsg)
                            }
                            true
                        },
                        hasClientName = clientName.isNotEmpty()
                    )
                }
            )
        }
    }
}


@ExperimentalKeyInput
@Composable
fun ClientTopBar(clientName: String, setClientName: (String) -> Unit, usernames: List<String>) {
    val (isEditName, _) = remember(clientName) { mutableStateOf(clientName.isEmpty()) }
    val (inputValue, setInputValue) = remember(clientName) { mutableStateOf(clientName) }

    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .fillMaxWidth()
    ) {
        val expandedNameListState = remember(clientName) { mutableStateOf(false) }
        Column {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Row(modifier = Modifier.height(60.dp)) {
                    if (isEditName) {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shortcuts {
                                    on(
                                        key = Key.Enter,
                                        callback = { setClientName.invoke(inputValue) }
                                    )
                                },
                            value = inputValue,
                            onValueChange = {
                                println("it = ${it}")
                                setInputValue(it)
                            },
                            placeholder = {
                                Text("Enter a name for this client")
                            },
                            backgroundColor = Color.Transparent,
                            trailingIcon = {
                                Row {
                                    Icon(
                                        modifier = Modifier.clickable {
                                            expandedNameListState.value = !expandedNameListState.value
                                        },
                                        imageVector = Icons.Filled.ChangeHistory
                                    )
                                    Icon(
                                        modifier = Modifier.clickable {
                                            setClientName.invoke(inputValue)
                                        },
                                        imageVector = Icons.Filled.Check
                                    )
                                }
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .align(Alignment.CenterStart),
                                text = "Client Name: $clientName"
                            )
                            Icon(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .align(Alignment.CenterEnd)
                                    .clickable {
                                        setClientName.invoke("")
                                    },
                                imageVector = Icons.Filled.ExitToApp
                            )
                        }
                    }
                }
            }
            if (expandedNameListState.value) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "In this conversation:",
                        textAlign = TextAlign.Center
                    )
                    LazyRowFor(usernames) { item ->
                        Button(
                            onClick = {
                                setClientName.invoke(item)
                            }
                        ) {
                            Text(item)
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalKeyInput
@Composable
fun ComposeMessageBar(onSendClicked: (String) -> Boolean, hasClientName: Boolean) {
    val (msgValue, setMsgValue) = remember { mutableStateOf("") }
    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Box {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .keyInputFilter { ev ->
                        (ev.type == KeyEventType.KeyDown && !ev.isShiftPressed && ev.key == Key.Enter).also {
                            if (it) {
                                val isSent = onSendClicked.invoke(msgValue)
                                if (isSent) {
                                    setMsgValue.invoke("")
                                }
                            }
                        }
                    },
                value = msgValue,
                onValueChange = setMsgValue,
                isErrorValue = !hasClientName,
                placeholder = {
                    val placeholderString =
                        if (hasClientName) "Type a message here" else "Enter a client name at the top"
                    Text(placeholderString)
                },
                backgroundColor = Color.Transparent,
            )
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {
                    val isSent = onSendClicked.invoke(msgValue)
                    if (isSent) {
                        setMsgValue.invoke("")
                    }
                },
                enabled = msgValue.isNotEmpty()
            ) {
                Icon(Icons.Filled.Send, tint = Color.White)
            }
        }
    }
}

@Composable
fun MessageList(msgs: List<Message>, clientName: String, forceDateTimeDisplay: Boolean = false) {
    val scrollState = rememberScrollState()
    remember(msgs.size) {
        scrollState.scrollTo(scrollState.maxValue)
    }
    ScrollableColumn(
        scrollState = scrollState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 0.dp),
    ) {
        for (msg in msgs) {
            DisplayMessage(
                msg = msg,
                clientName = clientName,
                isGroupChat = true,
                forceDateTimeDisplay = forceDateTimeDisplay
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yy.MM.dd")
val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm")

@Composable
fun DisplayMessage(
    msg: Message,
    clientName: String,
    isGroupChat: Boolean = false,
    forceDateTimeDisplay: Boolean = false
) {
    val (showDate, setShowDate) = remember { mutableStateOf(forceDateTimeDisplay) }
    val thisClientAuthor = msg.isThisClientAuthor(clientName)
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (thisClientAuthor) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .clickable(
                    onClick = {},
                    onDoubleClick = { // Toggle Show Date by Double Click
                        setShowDate.invoke(!showDate)
                    }
                ),
            backgroundColor = if (thisClientAuthor) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
            elevation = 0.dp,
            shape = RoundedCornerShape(
                topLeft = 15.dp,
                topRight = 15.dp,
                bottomRight = if (thisClientAuthor) 0.dp else 15.dp,
                bottomLeft = if (thisClientAuthor) 15.dp else 0.dp
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(all = 16.dp)) {
                val showAuthorName = isGroupChat && !thisClientAuthor
                if (showAuthorName || showDate) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (showAuthorName) {
                            Text(
                                modifier = Modifier.align(Alignment.CenterStart),
                                text = msg.author,
                                style = MaterialTheme.typography.caption,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (showDate) {
                            val date = remember(msg.timestamp) { dateFormatter.format(msg.timestamp) }
                            val time = remember(msg.timestamp) { timeFormatter.format(msg.timestamp) }
                            Text(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                text = "$date at $time",
                                style = MaterialTheme.typography.caption,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Row {
                    Text(text = msg.content)
                }
            }
        }
    }
}

data class Message(val content: String, val timestamp: LocalDateTime, val author: String) {
    fun isThisClientAuthor(clientName: String) = author == clientName
}
