package com.example.elderui.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elderui.core.api.EmergencyContact
import com.example.elderui.core.di.rememberAppViewModelFactory
import com.example.elderui.core.viewmodel.EmergencyContactViewModel
import com.example.elderui.ui.component.*

/**
 * 紧急联系人管理界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactScreen(
    onBack: () -> Unit = {}
) {
    val factory = rememberAppViewModelFactory()
    val viewModel: EmergencyContactViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val contacts by viewModel.contacts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<EmergencyContact?>(null) }

    var editName by remember { mutableStateOf("") }
    var editRelation by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editPriority by remember { mutableStateOf("1") }

    LaunchedEffect(Unit) {
        viewModel.getEmergencyContacts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("紧急联系人") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            if (contacts.size < 5) {  // 限制最多5个联系人
                FloatingActionButton(
                    onClick = {
                        editName = ""
                        editRelation = ""
                        editPhone = ""
                        editPriority = (contacts.size + 1).toString()
                        showAddDialog = true
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "添加联系人")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (error != null) {
                ErrorMessage(error!!) { viewModel.clearError() }
            }

            if (loading && contacts.isEmpty()) {
                LoadingIndicator()
            } else if (contacts.isEmpty()) {
                EmptyState(
                    message = "还没有添加紧急联系人\n建议添加家人或好友的联系方式",
                    modifier = Modifier.weight(1f)
                ) {
                    Button(onClick = {
                        editName = ""
                        editRelation = ""
                        editPhone = ""
                        editPriority = "1"
                        showAddDialog = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "添加")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("添加联系人")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "紧急报警时会自动通知这些联系人",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(contacts.sortedBy { it.priority }) { contact ->
                        EmergencyContactCard(
                            contact = contact,
                            onEdit = {
                                selectedContact = contact
                                editName = contact.name
                                editRelation = contact.relation
                                editPhone = contact.phone
                                editPriority = contact.priority.toString()
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedContact = contact
                                showDeleteDialog = true
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // 添加联系人对话框
    if (showAddDialog) {
        ContactEditDialog(
            title = "添加紧急联系人",
            name = editName,
            relation = editRelation,
            phone = editPhone,
            priority = editPriority,
            onNameChange = { editName = it },
            onRelationChange = { editRelation = it },
            onPhoneChange = { editPhone = it },
            onPriorityChange = { editPriority = it },
            onConfirm = {
                viewModel.addEmergencyContact(
                    name = editName,
                    relation = editRelation,
                    phone = editPhone,
                    priority = editPriority.toIntOrNull() ?: 1
                )
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // 编辑联系人对话框
    if (showEditDialog && selectedContact != null) {
        ContactEditDialog(
            title = "编辑联系人",
            name = editName,
            relation = editRelation,
            phone = editPhone,
            priority = editPriority,
            onNameChange = { editName = it },
            onRelationChange = { editRelation = it },
            onPhoneChange = { editPhone = it },
            onPriorityChange = { editPriority = it },
            onConfirm = {
                viewModel.updateEmergencyContact(
                    id = selectedContact!!.id,
                    name = editName,
                    relation = editRelation,
                    phone = editPhone,
                    priority = editPriority.toIntOrNull() ?: 1
                )
                showEditDialog = false
                selectedContact = null
            },
            onDismiss = {
                showEditDialog = false
                selectedContact = null
            }
        )
    }

    // 删除确认对话框
    if (showDeleteDialog && selectedContact != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedContact = null
            },
            title = { Text("删除联系人") },
            text = { Text("确认要删除 ${selectedContact?.name} 吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEmergencyContact(selectedContact!!.id)
                        showDeleteDialog = false
                        selectedContact = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    selectedContact = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 紧急联系人卡片
 */
@Composable
fun EmergencyContactCard(
    contact: EmergencyContact,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    CardContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = contact.priority.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column {
                        Text(
                            text = contact.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = contact.relation,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Phone,
                    contentDescription = "电话",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = contact.phone,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 联系人编辑对话框
 */
@Composable
fun ContactEditDialog(
    title: String,
    name: String,
    relation: String,
    phone: String,
    priority: String,
    onNameChange: (String) -> Unit,
    onRelationChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("姓名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = relation,
                    onValueChange = onRelationChange,
                    label = { Text("关系") },
                    placeholder = { Text("如：子女、配偶、朋友等") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("电话号码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = priority,
                    onValueChange = onPriorityChange,
                    label = { Text("优先级") },
                    placeholder = { Text("数字越小优先级越高") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = name.isNotBlank() && relation.isNotBlank() &&
                         phone.isNotBlank() && priority.toIntOrNull() != null
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}


