package io.github.raghavsatyadev.scus.compose.ui.dahboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.support.compose.components.DarkPreview
import io.github.raghavsatyadev.support.compose.components.LightPreview
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord

@Composable
fun MatchRecordItem(
  matchRecord: MatchRecord,
  onCopyClick: (MatchRecord) -> Unit,
  onDeleteClick: (MatchRecord) -> Unit,
) {
  Column {
    Text(
      text = matchRecord.team1Detail.teamName,
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.padding(16.dp),
    )
    Button(onClick = { onCopyClick(matchRecord) }) {}
  }
}

@DarkPreview
@LightPreview
@Composable
fun MatchRecordItemPreview2() {
  val matchRecord = getSampleMatchRecord()
  MatchRecordItem(matchRecord = matchRecord, onCopyClick = {}, onDeleteClick = {})
}

fun getSampleMatchRecord(): MatchRecord {
  return "{\"match_record_id\":\"eHBkXOi9Gxzqsd8dSP0D\",\"start_date_time\":1745345640000,\"end_date_time\":1745589475307,\"team_1\":{\"team_name\":\"Raghav\",\"runs\":21,\"wickets\":7,\"balls\":22},\"team_2\":{\"team_name\":\"Archan\",\"runs\":18,\"balls\":18},\"balls_per_inning\":72,\"is_first_inning_complete\":true,\"rrr_at_second_inning_start\":\"1.83\",\"status\":\"TEAM_1_WON\",\"location\":\"Ahmedabad \",\"match_admin_id\":\"r4pkT36tARfXSv2OLx1qP8xfWzl1\",\"local_update_date_time\":1745589475307,\"server_update_date_time\":1745345691000}"
    .toKotlinObject()
}

fun getSampleRecords(): ArrayList<MatchRecord> {
  val matchRecord = getSampleMatchRecord()
  val list = arrayListOf<MatchRecord>()
  repeat(10) { list.add(matchRecord) }
  return list
}
