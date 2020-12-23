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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import messaging_api.Author
import messaging_api.IMessagingAPI
import messaging_api.Message
import messaging_api.impl.DatabaseImpl
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalKeyInput::class)
fun main(args: Array<String>) {
    Window(
        size = IntSize(640, 1080),
    ) {
        val (clientName, setClientName) = remember { mutableStateOf("") }
        val (clientEMail, setClientEMail) = remember { mutableStateOf("") }

        val appWindow: AppWindow? = AppWindowAmbient.current
        remember(clientName) { // Updates the title of the window when the user changes
            val name = clientName.takeUnless { it.isEmpty() } ?: "<None Selected>"
            appWindow?.setTitle("Client: $name")
        }

        val api: IMessagingAPI = DatabaseImpl
        onActive {
            onDispose {
                api.close()
            }
        }
        val messages = api.messagesStateFlow.collectAsState(emptyList())

        val userInConversation = remember(messages.value) {
            val set = mutableSetOf<Author>()
            set.addAll(messages.value.map { it.author })
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
                        clientEMail = clientEMail,
                        setAuthorDetails = { name, email ->
                            setClientName(name)
                            setClientEMail(email)
                        },
                        usernames = userInConversation
                    )
                },
                bodyContent = {
                    WithConstraints {
                        val density = AmbientDensity.current
                        val width = with(density) { constraints.maxWidth.toDp() - it.start - it.end }
                        val height = with(density) { constraints.maxHeight.toDp() - it.top - it.bottom }
                            .takeIf { it.value >= 0 } ?: 0.dp


                        Box(modifier = Modifier.size(width, height)) {
                            MessageList(
                                msgs = messages.value,
                                clientEMail = clientEMail,
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
                                    content = it,
                                    timestamp = LocalDateTime.now(),
                                    author = Author(clientName, clientEMail)
                                )
                                GlobalScope.launch {
                                    api.sendMessage(newMsg)
                                }
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
fun ClientTopBar(
    clientName: String,
    clientEMail: String,
    setAuthorDetails: (String, String) -> Unit,
    usernames: List<Author>
) {
    val (isEditName, _) = remember(clientName) { mutableStateOf(clientName.isEmpty()) }
    val (inputNameValue, setInputNameValue) = remember(clientName) { mutableStateOf(clientName) }
    val (inputEMailValue, setInputEMailValue) = remember(clientEMail) { mutableStateOf(clientEMail) }

    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .fillMaxWidth()
    ) {
        Column {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                    //.height(60.dp)
                ) {
                    if (isEditName) {
                        Column {
                            EnterText(
                                value = inputNameValue,
                                onValueChange = setInputNameValue,
                                placeholderString = "Enter a name for this client",
                                onApply = {
                                    setAuthorDetails.invoke(inputNameValue, inputEMailValue)
                                }
                            )
                            EnterText(
                                value = inputEMailValue,
                                onValueChange = setInputEMailValue,
                                placeholderString = "Enter your e-mail",
                                onApply = {
                                    setAuthorDetails.invoke(inputNameValue, inputEMailValue)
                                }
                            )
                        }
                    } else {
                        Box(modifier = Modifier.height(60.dp).fillMaxWidth()) {
                            Text(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .align(Alignment.CenterStart),
                                text = "Client Name: $clientName [$clientEMail]"
                            )
                            Icon(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .align(Alignment.CenterEnd)
                                    .clickable {
                                        setAuthorDetails.invoke("", "")
                                    },
                                imageVector = Icons.Filled.ExitToApp
                            )
                        }
                    }
                }
            }
            if (isEditName) {
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
                                setAuthorDetails.invoke(item.name, item.email)
                            }
                        ) {
                            Text(item.name)
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalKeyInput
@Composable
fun EnterText(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderString: String,
    onApply: () -> Unit
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .shortcuts {
                on(
                    key = Key.Enter,
                    callback = { onApply.invoke() }
                )
            },
        value = value,
        onValueChange = {
            println("it = $it")
            onValueChange(it)
        },
        placeholder = {
            Text(text = placeholderString)
        },
        backgroundColor = Color.Transparent,
        trailingIcon = {
            Icon(
                modifier = Modifier.clickable {
                   onApply.invoke()
                },
                imageVector = Icons.Filled.Check
            )
        }
    )
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
fun MessageList(msgs: List<Message>, clientEMail: String, forceDateTimeDisplay: Boolean = false) {
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
                clientEMail = clientEMail,
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
    clientEMail: String,
    isGroupChat: Boolean = false,
    forceDateTimeDisplay: Boolean = false
) {
    val (showDate, setShowDate) = remember { mutableStateOf(forceDateTimeDisplay) }
    val thisClientAuthor = msg.isThisClientAuthor(clientEMail)
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
                            val (name, email) = msg.author
                            Text(
                                modifier = Modifier.align(Alignment.CenterStart),
                                text = "$name [$email]",
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

fun Message.isThisClientAuthor(clientEMail: String) = author.email == clientEMail
