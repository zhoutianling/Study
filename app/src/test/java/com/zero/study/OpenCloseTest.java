package com.zero.study;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 * <p>
 * 开闭原则
 *  所谓开放封闭原则就是软件实体应该对扩展开发，而对修改封闭。开放封闭原则是所有面向对象原则的核心。
 *  软件设计本身所追求的目标就是封装变化，降低耦合，而开放封闭原则正是对这一目标的最直接体现。
 * 开放封闭原则主要体现在两个方面：
 *  对扩展开放，意味着有新的需求或变化时，可以对现有代码进行扩展，以适应新的情况。
 *  对修改封闭，意味着类一旦设计完成，就可以独立其工作，而不要对类尽任何修改。
 *
 * 为什么要用到开放封闭原则呢？
 * 软件需求总是变化的，世界上没有一个软件的是不变的，因此对软件设计人员来说，必须在不需要对原有系统进行修改的情况下，
 * 实现灵活的系统扩展。
 *
 * 如何做到对扩展开放，对修改封闭呢？
 *  实现开放封闭的核心思想就是对抽象编程，而不对具体编程，因为抽象相对稳定。让类依赖于固定的抽象，所以对修改就是封闭的；
 *  而通过面向对象的继承和多态机制，可以实现对抽象体的继承，通过覆写其方法来改变固有行为，实现新的扩展方法，所以对于扩展就是开放的。
 * 对于违反这一原则的类，必须通过重构来进行改善。常用于实现的设计模式主要有Template Method模式和Strategy 模式。
 * 而封装变化，是实现这一原则的重要手段，将经常变化的状态封装为一个类。
 */
public class OpenCloseTest {
    @Test
    public void main() {
        BusinessMan businessMan = new BusinessMan();
        businessMan.handlerBusiness("licai");
    }

    class BankProcess {
        public void save() {
            System.out.println("存款");
        }

        public void query() {
            System.out.println("取款");
        }

        public void transfer() {
            System.out.println("转账");
        }
        public void transfer2() {
            System.out.println("理财");
        }
    }

    class BusinessMan {
        BankProcess process = new BankProcess();

        public void handlerBusiness(String type) {
            switch (type) {
                case "cunkuan":
                    process.save();
                    break;
                case "qukuan":
                    process.query();
                    break;
                case "zhuanzhang":
                    process.transfer();
                    break;
            }
        }


        //
        private IBankBusiness business = null;

        public void handlerBusiness2(String type) {
            switch (type) {
                case "cunkuan":
                    business = new SaveBusiness();
                    break;
                case "qukuan":
                    business = new QueryBusiness();
                    break;
                case "zhuanzhang":
                    business = new TransferBusiness();
                    break;
            }
            business.process();
        }
    }


    public interface IBankBusiness {
        void process();
    }

    public class SaveBusiness implements IBankBusiness {
        public void process() {//办理存款业务
            System.out.println("办理存款业务");
        }
    }

    public class QueryBusiness implements IBankBusiness {
        public void process() {
            System.out.println("办理取款业务");
        }
    }

    public class TransferBusiness implements IBankBusiness {
        public void process() {
            System.out.println("办理转账业务");
        }
    }    public class  TT implements IBankBusiness {
        public void process() {
            System.out.println("办理转账业务");
        }
    }

    //这样当业务变更时，只需要修改对应的业务实现类就可以，其他不相干的业务就不必修改。当业务增加，只需要增加业务的实现就可以了。
}