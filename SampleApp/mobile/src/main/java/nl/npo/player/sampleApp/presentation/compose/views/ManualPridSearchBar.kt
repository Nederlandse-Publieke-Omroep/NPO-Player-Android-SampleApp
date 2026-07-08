package nl.npo.player.sampleApp.presentation.compose.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualPridSearchBar(
    searchString: String,
    searchHint: String,
    onSearchStringChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchBar(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        inputField = {
            SearchBarDefaults.InputField(
                query = searchString,
                onQueryChange = onSearchStringChange,
                onSearch = {
                    val query = searchString.trim()
                    if (query.isNotBlank()) {
                        onSearch(query)
                    }
                },
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text(searchHint) },
            )
        },
        expanded = false,
        onExpandedChange = {},
    ) {}
}
