// IOnNewBookCallback.aidl
package com.zero.study.ipc;
import com.zero.study.ipc.Book;


interface IOnNewBookCallback {
  void callback(in Book newBook);
}
