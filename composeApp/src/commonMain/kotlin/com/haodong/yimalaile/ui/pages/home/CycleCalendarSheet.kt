package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.ui.components.CycleCalendarGrid
import com.haodong.yimalaile.ui.components.CycleCalendarLegend
import com.haodong.yimalaile.ui.components.SmallSpacer
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/**
 * Read-only cycle calendar bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CycleCalendarSheet(
    state: CycleState,
    phaseInfo: CyclePhaseInfo?,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
            Text(
                stringResource(Res.string.home_cycle_calendar),
                style = MaterialTheme.typography.titleLarge,
            )
            SmallSpacer(12)
            CycleCalendarLegend()
            SmallSpacer(16)
            CycleCalendarGrid(
                state = state,
                phaseInfo = phaseInfo,
                modifier = Modifier.height(400.dp),
            )
        }
    }
}
