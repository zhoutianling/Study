package com.zero.study.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexboxLayoutManager
import com.zero.base.fragment.BaseFragment
import com.zero.study.bean.HotWord
import com.zero.study.databinding.FragmentSecondBinding
import com.zero.study.ipc.Book
import com.zero.study.ipc.IBookManager
import com.zero.study.ipc.IOnNewBookCallback
import com.zero.study.service.BookManagerService
import com.zero.study.ui.adapter.HotWordAdapter
import kotlinx.coroutines.launch
import kotlin.random.Random

class SecondFragment : BaseFragment<FragmentSecondBinding>(FragmentSecondBinding::inflate) {

    private val mAdapter: HotWordAdapter by lazy {
        HotWordAdapter()
    }
    private var bookManager: IBookManager? = null
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            bookManager = IBookManager.Stub.asInterface(iBinder)
            try {
                bookManager?.registerListener(callback)
                Toast.makeText(activity, "服务已绑定", Toast.LENGTH_SHORT).show()
                bookManager?.autoAdd()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Toast.makeText(activity, "服务已停止", Toast.LENGTH_SHORT).show()
            bookManager = null
        }
    }
    private val callback: IOnNewBookCallback = object : IOnNewBookCallback.Stub() {

        override fun callback(newBook: Book) {
            Log.i("zzz", "NewBookArrived--->" + newBook.name)
            lifecycleScope.launch {
                mAdapter.add(HotWord(id = Random.nextInt(), name = newBook.name))
                mAdapter.notifyItemInserted(mAdapter.itemCount - 1)
            }
        }
    }


    override fun initView() {
        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.layoutManager = FlexboxLayoutManager(requireContext())
        binding.recyclerView.itemAnimator = null
        binding.swipeLayout.setOnRefreshListener {
            lifecycleScope.launch {
                binding.swipeLayout.isRefreshing = false
            }
        }
    }

    override fun initData() {
    }

    override fun setListener() {
    }


    override fun onStart() {
        super.onStart()
        requireActivity().bindService(Intent(activity, BookManagerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        try {
            requireActivity().stopService(Intent(activity, BookManagerService::class.java))
            requireActivity().unbindService(mConnection)
            bookManager?.unregisterListener(callback)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
}