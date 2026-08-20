import os
import re

path = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\ui\screens\dashboard\DashboardScreen.kt"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

chat_tab_new = """@Composable
fun ChatTab(chatViewModel: com.example.blood_donor.ui.viewmodels.ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val conversations by chatViewModel.conversations.androidx.compose.runtime.collectAsState()
    val isLoading by chatViewModel.isLoading.androidx.compose.runtime.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        chatViewModel.fetchConversations()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(20.dp)
    ) {
        Text("Messages", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = PrimaryRed)
            }
        } else if (conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No recent conversations", color = TextGray)
            }
        } else {
            LazyColumn {
                items(conversations) { convo ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            // TODO: Navigate to individual ChatScreen
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).background(PrimaryRed.copy(alpha=0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(convo.name.take(1).uppercase(), color = PrimaryRed, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(convo.name, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(convo.lastMessage, color = TextGray, fontSize = 14.sp, maxLines = 1)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
"""

# We need to replace the old ChatTab function.
content = re.sub(
    r'@Composable\s*fun ChatTab\(\)\s*\{\s*Box.*?Text\("Chat Tab UI Pending"\)\s*\}\s*\}',
    chat_tab_new,
    content,
    flags=re.DOTALL
)

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
