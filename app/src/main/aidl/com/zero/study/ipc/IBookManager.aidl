// IBookManager.aidl
package com.zero.study.ipc;
import com.zero.study.ipc.Book;
import com.zero.study.ipc.IOnNewBookCallback;

interface IBookManager {
    void autoAdd();
    void addBook(in Book book);
    List<Book> getList();
    void registerListener(IOnNewBookCallback listener);
    void unregisterListener(IOnNewBookCallback listener);
}
