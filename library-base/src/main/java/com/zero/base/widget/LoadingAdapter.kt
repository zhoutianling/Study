package com.zero.base.widget

import android.view.View

private const val HIDE_LOADING_STATUS_MSG = "hide_loading_status_msg"

/**

 * @date:2024/5/28 16:14
 * @path:com.toolkit.base.widget.LoadingAdapter
 */
class LoadingAdapter : Gloading.Adapter {
    override fun getView(holder: Gloading.Holder?, convertView: View?, status: Int): View {
        var loadingStatusView: LoadingView? = null
        if (convertView is LoadingView) {
            loadingStatusView = convertView
        }
        if (loadingStatusView == null) {
            loadingStatusView = LoadingView(holder?.context!!, holder.retryTask)
        }
        loadingStatusView.setStatus(status)
        val data = holder?.getData<Any>()
        val hideMsgView = HIDE_LOADING_STATUS_MSG == data
        loadingStatusView.setMsgViewVisibility(!hideMsgView)
        return loadingStatusView
    }
}
