package com.sbma.linkup.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sbma.linkup.R
import com.sbma.linkup.application.data.AppViewModelProvider
import com.sbma.linkup.card.Card
import com.sbma.linkup.card.CardViewModel
import com.sbma.linkup.presentation.components.EditCard
import com.sbma.linkup.presentation.ui.theme.LinkUpTheme
import com.sbma.linkup.presentation.ui.theme.YellowApp
import com.sbma.linkup.user.User
import com.sbma.linkup.user.UserViewModel
import com.sbma.linkup.util.toPictureResource
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun EditProfileScreenProvider(
    user: User,
    cards: List<Card>,
    onBackClick: () -> Unit,
    onSave: () -> Unit
) {
    val composableScope = rememberCoroutineScope()
    val userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val cardViewModel: CardViewModel = viewModel(factory = AppViewModelProvider.Factory)

    EditProfileScreen(
        user,
        cards,
        onBackClick = { onBackClick() }
    ) { cardsToInsert, cardsToUpdate, cardsToDelete ->
        println("cardsToInsert: $cardsToInsert")
        println("cardsToUpdate: $cardsToInsert")
        println("cardsToDelete: $cardsToDelete")
        composableScope.launch {
            cardsToInsert
                .forEach {
                    cardViewModel.saveItem(it).join()
                }
            cardsToUpdate
                .forEach {
                    cardViewModel.updateItem(it).join()
                }
            cardsToDelete
                .forEach {
                    cardViewModel.deleteItem(it).join()
                }
            userViewModel.syncRoomDatabase()
            onSave()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreenTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onBackClick: () -> Unit,
    onSave: () -> Unit
) {
    MediumTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(
                modifier = Modifier,
                onClick = { onBackClick() }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(
                modifier = Modifier,
                onClick = { onSave() }
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Save",
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                )
            }
        },
        scrollBehavior = scrollBehavior

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User,
    cards: List<Card>,
    onBackClick: () -> Unit,
    onSave: (
        cardsToInsert: List<Card>,
        cardsToUpdate: List<Card>,
        cardsToDelete: List<Card>,
    ) -> Unit,

    ) {
    val composableScope = rememberCoroutineScope()

    var userCards by rememberSaveable { mutableStateOf(cards) }
    var cardsToInsert by rememberSaveable { mutableStateOf<Map<UUID, Boolean>>(mapOf()) }
    var cardsToUpdate by rememberSaveable { mutableStateOf<Map<UUID, Boolean>>(mapOf()) }
    var cardsToDelete by rememberSaveable { mutableStateOf<Map<UUID, Card>>(mapOf()) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()



    Scaffold(
        topBar = {
            EditProfileScreenTopBar(
                scrollBehavior = scrollBehavior,
                onBackClick = onBackClick,
                onSave = {
                    composableScope.launch {
                        onSave(
                            cardsToInsert
                                .filter { it.value }
                                .map { it.key }
                                .mapNotNull { id -> userCards.find { it.id == id } },
                            cardsToUpdate
                                .filter { it.value }
                                .map { it.key }
                                .mapNotNull { id -> userCards.find { it.id == id } },
                            cardsToDelete
                                .map { it.value }
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomSheet { text, picture ->
                println("Clicked $text $picture")
                val copy = userCards.toMutableList()
                val id = UUID.randomUUID()
                copy.add(
                    Card(
                        id = id,
                        userId = user.id,
                        title = text,
                        value = "",
                        picture = picture
                    )
                )
                userCards = copy
                val cardsToInsertMutable = cardsToInsert.toMutableMap()
                cardsToInsertMutable[id] = true
                cardsToInsert = cardsToInsertMutable.toMap()
                println("CreateCard")
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { padding ->

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = user.picture,
                    contentDescription = "profile photo",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(50.dp))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = user.name, fontSize = 30.sp)
                Spacer(modifier = Modifier.height(8.dp))
//                Text(text = user.description, fontSize = 20.sp)
//                Spacer(modifier = Modifier.height(8.dp))
            }
            Column {
                userCards.forEachIndexed { index, card ->
                    EditCard(
                        card = card,
                        canEditTitle = card.title != "About me",
                        onCardModified = {
                            val userCardsMutable = userCards.toMutableList()
                            userCardsMutable[index] = it
                            userCards = userCardsMutable.toList()

                            val cardsToUpdateCopy = cardsToUpdate.toMutableMap()
                            cardsToUpdateCopy[it.id] = true
                            cardsToUpdate = cardsToUpdateCopy
                        },
                        onDelete = {
                            val copy = userCards.toMutableList()
                            copy.removeAt(index)
                            userCards = copy
                            val cardsToDeleteMutable = cardsToDelete.toMutableMap()
                            cardsToDeleteMutable[it.id] = it
                            cardsToDelete = cardsToDeleteMutable.toMap()
                        },
                        onPictureClick = {
                            // TODO: Open Bottom sheet and choose new image
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(onClick: (text: String, picture: String) -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {

        FloatingActionButton(
            onClick = { isSheetOpen = true },
            shape = CircleShape,
            containerColor = YellowApp,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }

    }
    if (isSheetOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen = false }) {
            SocialMediaList { text, picture ->
                println("Clicked! $text  $picture")
                onClick(text, picture)
                isSheetOpen = false

            }
        }
    }


}

@Composable
fun ProfileCardListItem(
    iconRes: Int,
    text: String
) {
    Column {
        ListItem(
            headlineContent = {
                Text(
                    text = text
                )
            },
            leadingContent = {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            }
        )
    }
}

data class NewCardItem(
    var category: String,
    var picture: String,
    var text: String
)

@Composable
fun CategoryHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    )

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SocialMediaList(onClick: (text: String, picture: String) -> Unit) {

    val newCardList = listOf(

        //Category Contacts
        NewCardItem(stringResource(R.string.contact), "phone", "Phone Number"),
        NewCardItem(stringResource(R.string.contact), "description", "Title"),
        NewCardItem(stringResource(R.string.contact), "address", "Address"),
        NewCardItem(stringResource(R.string.contact), "about", "About Me"),
        NewCardItem(stringResource(R.string.contact), "mail", "Email"),


        //Category Social Media
        NewCardItem(stringResource(R.string.social_media), "instagram", "Instagram"),
        NewCardItem(stringResource(R.string.social_media), "twitter", "Twitter"),
        NewCardItem(stringResource(R.string.social_media), "facebook", "Facebook"),
        NewCardItem(stringResource(R.string.social_media), "snapchat", "Snapchat"),
        NewCardItem(stringResource(R.string.social_media), "pinterest", "Pinterest"),
        NewCardItem(stringResource(R.string.social_media), "linkedin", "LinkedIn"),
        NewCardItem(stringResource(R.string.social_media), "telegram", "Telegram"),
        NewCardItem(stringResource(R.string.social_media), "tiktok", "TikTok"),
        NewCardItem(stringResource(R.string.social_media), "youtube", "Youtube"),
        NewCardItem(stringResource(R.string.social_media), "discord", "Discord"),
        NewCardItem(stringResource(R.string.social_media), "github", "Github"),
        NewCardItem(stringResource(R.string.social_media), "reddit", "Reddit"),
    )


    val categories = newCardList.groupBy { it.category }


    LazyColumn(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        categories.forEach { category ->
            stickyHeader {
                CategoryHeader(category.key)
            }
            items(category.value.toList().chunked(3)) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
//                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    for (rowItem in rowItems) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(25.dp)
                                .clickable { onClick(rowItem.text, rowItem.picture) }
                        ) {

                            Image(
                                painter = painterResource(rowItem.picture.toPictureResource()),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(48.dp),
//                                    .padding(8.dp),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            )
                            Text(
                                text = rowItem.text,
//                                modifier = Modifier.padding(start = 16.dp),
                                fontSize = 12.sp,

                                textAlign = TextAlign.Center,

//                                    .align(alignment = Alignment.CenterHorizontally)


//                                    modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }

    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val cards = remember {
        mutableListOf(
            Card(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Facebook",
                "https://facebook.com/something",
                "facebook"
            ),
            Card(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Instagram",
                "https://instagram.com/something",
                "instagram"
            ),
            Card(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Twitter",
                "https://twitter.com/something",
                "twitter"
            ),
            Card(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Twitch",
                "https://twitch.com/something",
                "twitch"
            ),
        )
    }
    val user =
        remember {
            mutableStateOf(
                User(
                    UUID.randomUUID(),
                    "Sebubebu",
                    email = "",
                    description = "UX/UI Designer",
                    picture = null
                )
            )
        }
    LinkUpTheme {
        EditProfileScreen(
            user.value,
            cards,
            onBackClick = {}
        ) { x, y, z ->

        }
    }
}

