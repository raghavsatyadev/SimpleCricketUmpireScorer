package io.github.raghavsatyadev.support.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class GenRecyclerAdapter<Model, Binding : ViewBinding, ViewHolder : GenObjectHolder<Model, Binding>>(
    var items: ArrayList<Model>,
) : RecyclerView.Adapter<ViewHolder>() {

    var itemClickListener: CustomClickListener? = null

    val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object :
                ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
                    0
                ) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    val adapter = recyclerView.adapter as GenRecyclerAdapter<*, *, *>
                    val from = viewHolder.absoluteAdapterPosition
                    val to = target.absoluteAdapterPosition
                    adapter.moveItem(from, to)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int,
                ) {
                    super.onSelectedChanged(viewHolder, actionState)

                    if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                        viewHolder?.itemView?.alpha = 0.5f
                    }
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                ) {
                    super.clearView(recyclerView, viewHolder)

                    viewHolder.itemView.alpha = 1.0f
                }
            }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    constructor() : this(ArrayList<Model>())

    fun startDragging(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setReorderingBehaviour(
        canReorder: Boolean,
        handleView: View,
        holder: GenObjectHolder<Model, Binding>,
    ) {
        if (canReorder) {
            handleView.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    startDragging(holder)
                }
                return@setOnTouchListener true
            }
        } else {
            handleView.setOnTouchListener(null)
        }
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return creatingViewHolder(parent, viewType, LayoutInflater.from(parent.context))
    }

    abstract fun creatingViewHolder(
        parent: ViewGroup,
        viewType: Int,
        from: LayoutInflater,
    ): ViewHolder

    final override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), getItemViewType(position), position, itemCount)
    }

    fun getItem(position: Int) = items[position]

    final override fun getItemCount() = items.size

    open fun replaceAll(models: ArrayList<Model>?) {
        if (models != null) {
            val oldItemCount = itemCount
            val newItemCount = models.size

            items.clear()
            items.addAll(models)

            if (oldItemCount == 0) {
                notifyItemRangeInserted(oldItemCount, newItemCount)
            } else if (newItemCount < oldItemCount) {
                notifyItemRangeChanged(0, newItemCount)
                notifyItemRangeRemoved(newItemCount, oldItemCount - newItemCount)
            } else {
                notifyItemRangeChanged(0, oldItemCount)
                notifyItemRangeInserted(oldItemCount, newItemCount - oldItemCount)
            }
        } else {
            val oldItemCount = itemCount
            items.clear()
            notifyItemRangeRemoved(0, oldItemCount)
        }
    }

    open fun replaceAll(models: List<Model>?) {
        models?.let { replaceAll(ArrayList(models)) }
    }

    open fun replaceItem(model: Model, position: Int) {
        if (itemCount > position) {
            items[position] = model
            notifyItemChanged(position)
        } else {
            items.add(model)
            notifyItemInserted(position)
        }
    }

    open fun addAll(models: ArrayList<Model>) {
        val position = itemCount
        items.addAll(models)
        notifyItemRangeInserted(position, models.size)
    }

    open fun deleteAll() {
        val itemCount = itemCount
        if (itemCount != 0) {
            items.clear()
            notifyItemRangeRemoved(0, itemCount)
        }
    }

    open fun addItem(model: Model, position: Int) {
        items.add(position, model)
        notifyItemInserted(position)
    }

    open fun setItem(model: Model, position: Int) {
        items[position] = model
        notifyItemChanged(position)
    }

    open fun addItem(model: Model) {
        items.add(model)
        notifyItemInserted(itemCount - 1)
    }

    open fun deleteItem(position: Int) {
        if (position in 0 until itemCount) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun moveItem(from: Int, to: Int) {
        val fromEmoji = items[from]
        items.removeAt(from)
        if (to < from) {
            items.add(to, fromEmoji)
        } else {
            items.add(to - 1, fromEmoji)
        }
        notifyItemMoved(from, to)
    }
}