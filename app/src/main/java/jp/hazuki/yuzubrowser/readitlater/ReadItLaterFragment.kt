/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.readitlater

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.view.*
import android.widget.TextView
import jp.hazuki.yuzubrowser.BrowserActivity
import jp.hazuki.yuzubrowser.Constants
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.FontUtils
import jp.hazuki.yuzubrowser.utils.UrlUtils
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener
import java.text.SimpleDateFormat
import java.util.*

class ReadItLaterFragment : Fragment(), OnRecyclerListener, ActionMode.Callback {

    private lateinit var adapter: ReaderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recycler_view, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        if (view != null) {
            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.addItemDecoration(DividerItemDecoration(activity))

            adapter = ReaderAdapter(activity, getList(), this)
            recyclerView.adapter = adapter
        }
    }

    override fun onRecyclerItemClicked(v: View?, position: Int) {
        startActivity(Intent(activity, BrowserActivity::class.java).apply {
            action = Constants.intent.ACTION_OPEN_DEFAULT
            data = ReadItLaterProvider.getReadUri(adapter[position].time)
        })
        activity.finish()
    }

    override fun onRecyclerItemLongClicked(v: View?, position: Int): Boolean {
        activity.startActionMode(this)
        adapter.isMultiSelectMode = true
        adapter.setSelect(position, true)
        return true
    }

    override fun onActionItemClicked(p0: ActionMode, menu: MenuItem): Boolean {
        return when (menu.itemId) {
            R.id.delete -> {
                val items = adapter.selectedItems
                val resolver = activity.contentResolver
                items.reversed()
                        .map { adapter.remove(it) }
                        .forEach { resolver.delete(ReadItLaterProvider.getEditUri(it.time), null, null) }

                adapter.notifyDataSetChanged()
                p0.finish()
                return true
            }
            else -> false
        }
    }

    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        actionMode.menuInflater.inflate(R.menu.action_delete, menu)
        return true
    }

    override fun onPrepareActionMode(p0: ActionMode?, menu: Menu?): Boolean = false

    override fun onDestroyActionMode(p0: ActionMode?) {
        adapter.isMultiSelectMode = false
    }

    private fun getList(): MutableList<ReadItem> {
        val cursor = activity.contentResolver.query(ReadItLaterProvider.EDIT_URI, null, null, null, null)
        val list = ArrayList<ReadItem>()
        while (cursor.moveToNext()) {
            list.add(ReadItem(
                    cursor.getLong(ReadItLaterProvider.COL_TIME),
                    cursor.getString(ReadItLaterProvider.COL_URL),
                    cursor.getString(ReadItLaterProvider.COL_TITLE)))
        }
        cursor.close()
        return list
    }

    private class ReaderAdapter(context: Context, list: MutableList<ReadItem>, listener: OnRecyclerListener)
        : ArrayRecyclerAdapter<ReadItem, ReaderAdapter.ReaderViewHolder>(context, list, listener) {
        val date = DateFormat.getDateFormat(context)!!
        @SuppressLint("SimpleDateFormat")
        val time = SimpleDateFormat("kk:mm")

        override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): ReaderViewHolder {
            return ReaderViewHolder(inflater.inflate(R.layout.read_it_later_item, parent, false), this)
        }

        override fun onBindViewHolder(holder: ReaderViewHolder, item: ReadItem, position: Int) {
            val d = Date(item.time)
            holder.time.text = date.format(d) + " " + time.format(d)

            holder.foreground.visibility = if (isMultiSelectMode && isSelected(position)) View.VISIBLE else View.INVISIBLE
        }

        private class ReaderViewHolder(view: View, adapter: ReaderAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<ReadItem>(view, adapter) {
            val title = view.findViewById<TextView>(R.id.titleTextView)!!
            val url = view.findViewById<TextView>(R.id.urlTextView)!!
            val time = view.findViewById<TextView>(R.id.timeTextView)!!
            val foreground = view.findViewById<View>(R.id.foreground)!!

            init {
                val font = AppData.font_size.readItLater.get()
                if (font >= 0) {
                    title.textSize = FontUtils.getTextSize(font).toFloat()
                    val small = FontUtils.getSmallerTextSize(font).toFloat()
                    url.textSize = small
                    time.textSize = small
                }
            }

            override fun setUp(item: ReadItem) {
                super.setUp(item)
                title.text = item.title
                url.text = UrlUtils.decodeUrlHost(item.url)
            }
        }
    }
}