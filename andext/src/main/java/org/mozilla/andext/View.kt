package org.mozilla.andext

import android.util.SparseArray
import android.view.View
import androidx.core.util.containsKey
import androidx.recyclerview.widget.RecyclerView


val RecyclerView.ViewHolder.viewMap: SparseArray<View>
    get() {
        var map = itemView.getTag(R.id.recyclerview_viewholder_map)
        if(map == null || map !is SparseArray<*>){
            map = SparseArray<View>()
            itemView.setTag(R.id.recyclerview_viewholder_map, map)
        }

        return map as SparseArray<View>
    }


fun <T : View> RecyclerView.ViewHolder.findViewOften(viewId: Int): T {
    if (viewMap.containsKey(viewId)) {
        return viewMap.get(viewId) as T
    }

    val view: T = itemView.findViewById(viewId)
    viewMap.put(viewId, view)
    return view
}

