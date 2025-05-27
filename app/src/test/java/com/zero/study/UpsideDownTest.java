package com.zero.study;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 * <p>
 * 依赖倒置原则
 * 定义：高层模块不应该依赖低层模块，二者都应该依赖其抽象；抽象不应该依赖细节；细节应该依赖抽象。
 * 问题由来：类A直接依赖类B，假如要将类A改为依赖类C，则必须通过修改类A的代码来达成。
 * 这种场景下，类A一般是高层模块，负责复杂的业务逻辑；类B和类C是低层模块，负责基本的原子操作；假如修改类A，会给程序带来不必要的风险。
 * 解决方案：将类A修改为依赖接口I，类B和类C各自实现接口I，类A通过接口I间接与类B或者类C发生联系，则会大大降低修改类A的几率。
 * 依赖倒置原则基于这样一个事实：相对于细节的多变性，抽象的东西要稳定的多。
 * 以抽象为基础搭建起来的架构比以细节为基础搭建起来的架构要稳定的多。
 * 在java中，抽象指的是接口或者抽象类，细节就是具体的实现类，使用接口或者抽象类的目的是制定好规范和契约，而不去涉及任何具体的操作，
 * 把展现细节的任务交给他们的实现类去完成。
 *  依赖倒置原则的核心思想是面向接口编程，我们依旧用一个例子来说明面向接口编程比相对于面向实现编程好在什么地方。
 *  场景是这样的，母亲给孩子讲故事，
 *  只要给她一本书，她就可以照着书给孩子讲故事了。代码如下：
 */
public class UpsideDownTest {
    @Test
    public void main() {

        Mother mother = new Mother();
        mother.read(new Book());



//        Father father = new Father();
//        father.read(new Book2());
//        father.read(new NewsPaper2());

    }



    class Book {
        public String getContent() {
            return "很久很久以前有三个小矮人……";
        }
    }

    class NewsPaper {
        public String getContent() {
            return "插播一条新闻。。。";
        }
    }

    class Mother {
        public void read(Book book) {
            System.out.println("妈妈开始讲故事。。。" + book.getContent());
        }
    }

    class Father{
        public void read(IReader reader){
            System.out.println("妈妈开始讲故事。。。" + reader.getContent());
        }
    }

    interface IReader {
        public String getContent();
    }

    class Book2 implements IReader {
        @Override
        public String getContent() {
            return "很久很久以前有三个小矮人。。。";
        }
    }

    class NewsPaper2 implements IReader {

        @Override
        public String getContent() {
            return "插播一条新闻。。。。";
        }
    }
}

//这样修改后，无论以后怎样扩展Client类，都不需要再修改Mother类了
//依赖倒置原则的核心就是要我们面向接口编程，理解了面向接口编程，也就理解了依赖倒置。
