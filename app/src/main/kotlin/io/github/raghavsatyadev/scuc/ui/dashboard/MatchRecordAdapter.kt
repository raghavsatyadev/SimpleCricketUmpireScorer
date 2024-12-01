package io.github.raghavsatyadev.scuc.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.raghavsatyadev.scuc.R
import io.github.raghavsatyadev.scuc.databinding.ItemMatchRecordBinding
import io.github.raghavsatyadev.support.extensions.ResourceExtensions.getAttrColor
import io.github.raghavsatyadev.support.list.GenRecyclerAdapter
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord

class MatchRecordAdapter(context: Context) :
    GenRecyclerAdapter<MatchRecord, ItemMatchRecordBinding, MatchRecordHolder>() {
    val won: String = context.getString(R.string.won)
    val lost: String = context.getString(R.string.lost)
    val draw: String = context.getString(R.string.draw)
    val inProgress: String = context.getString(R.string.in_progress)
    val lostColor: Int = context.getColor(android.R.color.holo_red_dark)
    val winColor: Int = context.getColor(android.R.color.holo_green_dark)
    val drawColor: Int =
        context.getColor(android.R.color.holo_blue_dark)
    val inProgressColor: Int =
        context.getAttrColor(com.google.android.material.R.attr.colorSurfaceInverse)

    override fun creatingViewHolder(
        parent: ViewGroup,
        viewType: Int,
        from: LayoutInflater,
    ): MatchRecordHolder {
        return MatchRecordHolder.getInstance(
            ItemMatchRecordBinding.inflate(from, parent, false),
            itemClickListener,
            won,
            lost,
            draw,
            inProgress,
            lostColor,
            winColor,
            drawColor,
            inProgressColor
        )
    }
}