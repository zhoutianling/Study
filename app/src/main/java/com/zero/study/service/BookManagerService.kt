package com.zero.study.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.RemoteException
import android.os.SystemClock
import android.util.Log
import com.zero.study.NotificationManager
import com.zero.study.R
import com.zero.study.db.BookDao
import com.zero.study.db.DbManager
import com.zero.study.ipc.Book
import com.zero.study.ipc.IBookManager
import com.zero.study.ipc.IOnNewBookCallback
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

open class BookManagerService : Service() {
    private val mBookList = CopyOnWriteArrayList<Book>()
    private val mListenerList = CopyOnWriteArrayList<IOnNewBookCallback>()

    /**
     *  当 Service 第一次创建时调用。可以执行一些初始化操作。
     */
    override fun onCreate() {
        super.onCreate()
        mBookList.add(Book(name = "android群英传"))
        mBookList.add(Book(name = "android开发艺术探索"))
        NotificationManager.showBackgroundNotification(this@BookManagerService, getString(R.string.app_name), "后台运行中，请勿退出...")
    }


    /**
     *  当另一个组件（如 Activity）通过 startService() 方法请求启动 Service 时调用。在这里，您可以处理 Service 的逻辑
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }


    private val mBinder: Binder = object : IBookManager.Stub() {
        override fun autoAdd() {
            Thread(ProductBookThread()).start()
        }

        override fun addBook(book: Book) {
            mBookList.add(book)
        }

        override fun getList(): List<Book> {
            return mBookList
        }

        override fun registerListener(listener: IOnNewBookCallback) {
            Log.d("zzz", "registerListener: $listener")
            if (!mListenerList.contains(listener)) {
                mListenerList.add(listener)
            }
        }

        override fun unregisterListener(listener: IOnNewBookCallback) {
            Log.d("zzz", "unregisterListener: $listener")
            mListenerList.remove(listener)
        }
    }

    /**
     * 当另一个组件（如 Activity）通过 bindService() 方法请求绑定 Service 时调用。如果您的 Service 不支持绑定，则返回 null。
     */
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    /**
     * 当所有客户端都与 Service 解绑时调用。如果 onUnbind() 返回 true，则 onRebind() 方法将在客户端重新绑定时调用。
     */
    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    private inner class ProductBookThread : Runnable {
        override fun run() {
            for (i in 0..99) {
                val book = Book(name = randomString())
                mBookList.add(book)
                //第一种方式
                for (listener in mListenerList) {
                    try {
                        listener.callback(book)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
                SystemClock.sleep(2000)
            }
        }
    }

    private fun randomString(): String {
        val charPool: List<Char> = ('A'..'Z') + ('0'..'9')
        val randomString = (1..Random.nextInt(1, 11)).map { _ -> charPool[Random.nextInt(0, charPool.size)] }.joinToString("")
        return randomString
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("zzz", "onDestroy")
    }
}
