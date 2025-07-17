package io.github.raghavsatyadev.scus.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.scus.databinding.ItemMatchRecordBinding
import io.github.raghavsatyadev.support.extensions.ResourceExtensions.getAttrColor
import io.github.raghavsatyadev.support.list.GenRecyclerAdapter
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord

class MatchRecordAdapter(context: Context) : GenRecyclerAdapter<MatchRecord, ItemMatchRecordBinding, MatchRecordHolder>() {
    private val won: String = context.getString(R.string.won)
    private val lost: String = context.getString(R.string.lost)
    private val draw: String = context.getString(R.string.draw)
    private val inProgress: String = context.getString(R.string.in_progress)
    private val lostColor: Int = context.getColor(android.R.color.holo_red_dark)
    private val winColor: Int = context.getColor(android.R.color.holo_green_dark)
    private val drawColor: Int = context.getColor(android.R.color.holo_blue_dark)
    private val inProgressColor: Int =
        context.getAttrColor(com.google.android.material.R.attr.colorSurfaceInverse)

    override fun creatingViewHolder(
        parent: ViewGroup,
        viewType: Int,
        from: LayoutInflater,
    ): MatchRecordHolder {
        return MatchRecordHolder.getInstance(
            ItemMatchRecordBinding.inflate(
                from,
                parent,
                false
            ),
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
