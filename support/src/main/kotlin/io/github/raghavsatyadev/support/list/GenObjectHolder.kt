package io.github.raghavsatyadev.support.list

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class GenObjectHolder<Model, Binding : ViewBinding>(
    val binding: Binding,
    var listener: CustomClickListener? = null,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.setOnClickListener {
            listener?.onItemClick(layoutPosition, it, false)
        }
    }

    abstract fun bind(model: Model, itemViewType: Int, position: Int, itemCount: Int)
}