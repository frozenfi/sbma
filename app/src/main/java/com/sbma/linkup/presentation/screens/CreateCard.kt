package com.sbma.linkup.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sbma.linkup.R
import com.sbma.linkup.presentation.ui.theme.LinkUpTheme

data class CreateCardData(
    val title: String,
    val value: String
)

@Composable
fun CreateCard(onSubmit: (value: CreateCardData) -> Unit) {
    var title by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(10.dp),
        content = {
            Text(
                text = "Create a new card",
                fontSize = 25.sp,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            ListItem(
                headlineContent = {
                    TextField(
                        value = title,
                        onValueChange = {},
                        label = { Text("Title") },
                    )

                },
                supportingContent = {
                    TextField(
                        value = value,
                        onValueChange = {},
                        label = { Text("Value") },
                    )
                },
                leadingContent = {
                    Image(
                        painterResource(R.drawable.linkedin),
                        contentDescription = "Linkedin",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                    )
                }
            )

            Button(
                onClick = {
                    onSubmit(
                        CreateCardData(
                            title = title,
                            value = value,
                        )
                    )
                },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Localized description",
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Add")
            }
        }
    )

}

@Preview(showBackground = true)
@Composable
fun CreateCardPreview() {
    LinkUpTheme {
        CreateCard {

        }
    }
}