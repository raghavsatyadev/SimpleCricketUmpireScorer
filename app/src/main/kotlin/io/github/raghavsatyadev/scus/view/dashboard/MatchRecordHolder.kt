package io.github.raghavsatyadev.scus.view.dashboard

import android.annotation.SuppressLint
import io.github.raghavsatyadev.scus.databinding.ItemMatchRecordBinding
import io.github.raghavsatyadev.support.extensions.ViewExtensions.gone
import io.github.raghavsatyadev.support.extensions.ViewExtensions.visible
import io.github.raghavsatyadev.support.list.CustomClickListener
import io.github.raghavsatyadev.support.list.GenObjectHolder
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.getMatchTimings
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.getTeam1FormattedScore
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.getTeam2FormattedScore
import io.github.raghavsatyadev.support.models.db.match_record.MatchStatus

class MatchRecordHolder(
  binding: ItemMatchRecordBinding,
  itemClickListener: CustomClickListener?,
  private val won: String,
  private val lost: String,
  private val draw: String,
  private val inProgress: String,
  private val lostColor: Int,
  private val winColor: Int,
  private val drawColor: Int,
  private val inProgressColor: Int,
) : GenObjectHolder<MatchRecord, ItemMatchRecordBinding>(binding, itemClickListener) {
  companion object {
    fun getInstance(
      binding: ItemMatchRecordBinding,
      itemClickListener: CustomClickListener?,
      won: String,
      lost: String,
      draw: String,
      inProgress: String,
      lostColor: Int,
      winColor: Int,
      drawColor: Int,
      inProgressColor: Int,
    ): MatchRecordHolder {
      return MatchRecordHolder(
        binding,
        itemClickListener,
        won,
        lost,
        draw,
        inProgress,
        lostColor,
        winColor,
        drawColor,
        inProgressColor,
      )
    }
  }

  init {
    with(binding) {
      btnCopy.setOnClickListener { itemClickListener?.onItemClick(layoutPosition, btnCopy, false) }
      btnDelete.setOnClickListener {
        itemClickListener?.onItemClick(layoutPosition, btnDelete, false)
      }
    }
  }

  @SuppressLint("SetTextI18n")
  override fun bind(model: MatchRecord, itemViewType: Int, position: Int, itemCount: Int) {
    with(binding) {
      with(model) {
        txtTeam1Name.text = team1Detail.teamName
        txtTeam2Name.text = team2Detail.teamName
        txtTeam1Score.text = getTeam1FormattedScore()
        txtTeam2Score.text = getTeam2FormattedScore()

        txtMatchLocation.text = location
        txtMatchDuration.text = getMatchTimings()

        when (status) {
          MatchStatus.TEAM_1_WON -> {
            txtTeam1MatchStatus.text = won
            txtTeam2MatchStatus.text = lost
            txtTeam1MatchStatus.setTextColor(winColor)
            txtTeam2MatchStatus.setTextColor(lostColor)
            txtCombinedMatchStatus.gone()
            groupMatchStatus.visible()
          }

          MatchStatus.TEAM_2_WON -> {
            txtTeam1MatchStatus.text = lost
            txtTeam2MatchStatus.text = won
            txtTeam1MatchStatus.setTextColor(lostColor)
            txtTeam2MatchStatus.setTextColor(winColor)
            txtCombinedMatchStatus.gone()
            groupMatchStatus.visible()
          }

          MatchStatus.DRAW -> {
            txtCombinedMatchStatus.visible()
            groupMatchStatus.gone()
            txtCombinedMatchStatus.text = draw
            txtCombinedMatchStatus.setTextColor(drawColor)
          }

          else -> {
            txtCombinedMatchStatus.visible()
            groupMatchStatus.gone()
            txtCombinedMatchStatus.text = inProgress
            txtCombinedMatchStatus.setTextColor(inProgressColor)
          }
        }
      }
    }
  }
}
