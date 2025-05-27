package com.zero.base.widget

import android.R
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.concurrent.Volatile

class Gloading private constructor() {
    private var mAdapter: Adapter? = null

    /**
     * Provides view to show current loading status
     */
    interface Adapter {
        /**
         * get view for current status
         * @param holder Holder
         * @param convertView The old view to reuse, if possible.
         * @param status current status
         * @return status view to show. Maybe convertView for reuse.
         * @see Holder
         */
        fun getView(holder: Holder?, convertView: View?, status: Int): View?
    }

    /**
     * Gloading(loading status view) wrap the whole activity
     * wrapper is android.R.id.content
     * @param activity current activity object
     * @return holder of Gloading
     */
    fun wrap(activity: Activity): Holder {
        val wrapper = activity.findViewById<ViewGroup>(R.id.content)
        return Holder(mAdapter, activity, wrapper)
    }

    /**
     * Gloading(loading status view) wrap the specific view.
     * @param view view to be wrapped
     * @return Holder
     */
    fun wrap(view: View): Holder {
        val wrapper = FrameLayout(view.context)
        val lp = view.layoutParams
        if (lp != null) {
            wrapper.layoutParams = lp
        }
        if (view.parent != null) {
            val parent = view.parent as ViewGroup
            val index = parent.indexOfChild(view)
            parent.removeView(view)
            parent.addView(wrapper, index)
        }
        val newLp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        wrapper.addView(view, newLp)
        return Holder(mAdapter, view.context, wrapper)
    }

    /**
     * loadingStatusView shows cover the view with the same LayoutParams object
     * this method is useful with RelativeLayout and ConstraintLayout
     * @param view the view which needs show loading status
     * @return Holder
     */
    fun cover(view: View): Holder {
        val parent = view.parent
            ?: throw RuntimeException("view has no parent to show gloading as cover!")
        val viewGroup = parent as ViewGroup
        val wrapper = FrameLayout(view.context)
        viewGroup.addView(wrapper, view.layoutParams)
        return Holder(mAdapter, view.context, wrapper)
    }

    fun initDefault(adapter: Adapter?) {
        default!!.mAdapter = adapter
    }

    /**
     * Gloading holder<br></br>
     * create by [Gloading.wrap] or [Gloading.wrap]<br></br>
     * the core API for showing all status view
     */
    class Holder(private val mAdapter: Adapter?, context: Context, wrapper: ViewGroup) {
        val context: Context = context

        /**
         * get retry task
         * @return retry task
         */
        var retryTask: Runnable? = null
            private set
        private var mCurStatusView: View? = null

        /**
         * get wrapper
         * @return container of gloading
         */
        private val wrapper: ViewGroup = wrapper
        private var curState = 0
        private val mStatusViews = SparseArray<View>(4)
        private var mData: Any? = null

        /**
         * set retry task when user click the retry button in load failed page
         * @param task when user click in load failed UI, run this task
         * @return this
         */
        fun withRetry(task: Runnable?): Holder {
            retryTask = task
            return this
        }

        /**
         * set extension data
         * @param data extension data
         * @return this
         */
        fun withData(data: Any?): Holder {
            this.mData = data
            return this
        }

        /** show UI for status: [.STATUS_LOADING]  */
        fun showLoading() {
            showLoadingStatus(STATUS_LOADING)
        }

        /** show UI for status: [.STATUS_LOAD_SUCCESS]  */
        fun showLoadSuccess() {
            showLoadingStatus(STATUS_LOAD_SUCCESS)
        }

        /** show UI for status: [.STATUS_LOAD_FAILED]  */
        fun showLoadFailed() {
            showLoadingStatus(STATUS_LOAD_FAILED)
        }

        /** show UI for status: [.STATUS_EMPTY_DATA]  */
        fun showEmpty() {
            showLoadingStatus(STATUS_EMPTY_DATA)
        }

        /**
         * Show specific status UI
         * @param status status
         * @see .showLoading
         * @see .showLoadFailed
         * @see .showLoadSuccess
         * @see .showEmpty
         */
        private fun showLoadingStatus(status: Int) {
            if (curState == status || !validate()) {
                return
            }
            curState = status
            //first try to reuse status view
            var convertView = mStatusViews[status]
            if (convertView == null) {
                //secondly try to reuse current status view
                convertView = mCurStatusView
            }
            try {
                //call customer adapter to get UI for specific status. convertView can be reused
                val view = mAdapter!!.getView(this, convertView, status)
                if (view == null) {
                    printLog(mAdapter.javaClass.name + ".getView returns null")
                    return
                }
                if (view !== mCurStatusView || wrapper!!.indexOfChild(view) < 0) {
                    if (mCurStatusView != null) {
                        wrapper!!.removeView(mCurStatusView)
                    }
                    view.elevation = Float.MAX_VALUE
                    wrapper!!.addView(view)
                    val lp = view.layoutParams
                    if (lp != null) {
                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                        lp.height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                } else if (wrapper.indexOfChild(view) != wrapper.childCount - 1) {
                    // make sure loading status view at the front
                    view.bringToFront()
                }
                mCurStatusView = view
                mStatusViews.put(status, view)
            } catch (e: Exception) {
                if (DEBUG) {
                    e.printStackTrace()
                }
            }
        }

        private fun validate(): Boolean {
            if (mAdapter == null) {
                printLog("Gloading.Adapter is not specified.")
            }
            if (context == null) {
                printLog("Context is null.")
            }
            if (wrapper == null) {
                printLog("The mWrapper of loading status view is null.")
            }
            return mAdapter != null && context != null && wrapper != null
        }

        /**
         *
         * get extension data
         * @param <T> return type
         * @return data
        </T> */
        fun <T> getData(): T? {
            try {
                return mData as T?
            } catch (e: Exception) {
                if (DEBUG) {
                    e.printStackTrace()
                }
            }
            return null
        }
    }

    companion object {
        const val STATUS_LOADING: Int = 1
        const val STATUS_LOAD_SUCCESS: Int = 2
        const val STATUS_LOAD_FAILED: Int = 3
        const val STATUS_EMPTY_DATA: Int = 4

        @Volatile
        private var mDefault: Gloading? = null
        private var DEBUG = false

        /**
         * set debug mode or not
         * @param debug true:debug mode, false:not debug mode
         */
        fun debug(debug: Boolean) {
            DEBUG = debug
        }

        /**
         * Create a new Gloading different from the default one
         * @param adapter another adapter different from the default one
         * @return Gloading
         */
        fun from(adapter: Adapter?): Gloading {
            val gloading = Gloading()
            gloading.mAdapter = adapter
            return gloading
        }

        val default: Gloading?
            /**
             * get default Gloading object for global usage in whole app
             * @return default Gloading object
             */
            get() {
                if (mDefault == null) {
                    synchronized(Gloading::class.java) {
                        if (mDefault == null) {
                            mDefault = Gloading()
                        }
                    }
                }
                return mDefault
            }

        /**
         * init the default loading status view creator ([Adapter])
         * @param adapter adapter to create all status views
         */


        private fun printLog(msg: String) {
            if (DEBUG) {
                Log.e("Gloading", msg)
            }
        }
    }
}
