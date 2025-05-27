package com.zero.study;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 * <p>
 * 接口隔离原则
 */
public class IsolationInterfaceTest {
    @Test
    public void main() {
        A a = new A();
        a.depend1(new B());
        a.depend2(new B());
        a.depend3(new B());

        C c = new C();
        c.depend1(new D());
        c.depend2(new D());
        c.depend3(new D());
    }

    interface I {
        void method1();

        void method2();

        void method3();

        void method4();

        void method5();
    }

    class A {
        public void depend1(I i) {
            i.method1();
        }

        public void depend2(I i) {
            i.method2();
        }

        public void depend3(I i) {
            i.method3();
        }
    }

    class B implements I {

        @Override
        public void method1() {
            System.out.println("类B实现接口I的方法1");
        }

        @Override
        public void method2() {
            System.out.println("类B实现接口I的方法2");
        }

        @Override
        public void method3() {
            System.out.println("类B实现接口I的方法3");
        }

        @Override
        public void method4() {

        }

        @Override
        public void method5() {

        }
    }

    class C {
        public void depend1(I i) {
            i.method1();
        }

        public void depend2(I i) {
            i.method2();
        }

        public void depend3(I i) {
            i.method3();
        }
    }

    class D implements I {

        @Override
        public void method1() {
            System.out.println("类D实现接口I的方法1");
        }

        @Override
        public void method2() {

        }

        @Override
        public void method3() {

        }

        @Override
        public void method4() {
            System.out.println("类D实现接口I的方法4");
        }

        @Override
        public void method5() {
            System.out.println("类D实现接口I的方法5");
        }
    }

    interface I1 {
        void method1();
    }

    interface I2 {
        void method2();

        void method3();
    }

    interface I3 {
        void method4();

        void method5();
    }

    class A1 {
        public void depend1(I1 i) {
            i.method1();
        }

        public void depend2(I2 i) {
            i.method2();
        }

        public void depend3(I2 i) {
            i.method3();
        }
    }

    class B1 implements I1,I2{

        @Override
        public void method1() {
            System.out.println("类B实现接口I的方法1");
        }

        @Override
        public void method2() {
            System.out.println("类B实现接口I2的方法2");
        }

        @Override
        public void method3() {
            System.out.println("类B实现接口I2的方法3");
        }
    }
    class D1 implements I1,I3{
        @Override
        public void method1() {
            System.out.println("类D实现接口I1的方法1");
        }

        @Override
        public void method4() {
            System.out.println("类D实现接口I3的方法4");
        }

        @Override
        public void method5() {
            System.out.println("类D实现接口I3的方法5");
        }
    }


    //接口尽量小，但是要有限度。对接口进行细化可以提高程序设计灵活性是不挣的事实，但是如果过小，则会造成接口数量过多，使设计复杂化。所以一定要适度。
    //为依赖接口的类定制服务，只暴露给调用的类它需要的方法，它不需要的方法则隐藏起来。只有专注地为一个模块提供定制服务，才能建立最小的依赖关系。
    //提高内聚，减少对外交互。使接口用最少的方法去完成最多的事情。
    //运用接口隔离原则，一定要适度，接口设计的过大或过小都不好。
}