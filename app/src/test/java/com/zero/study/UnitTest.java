package com.zero.study;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * 单一职责原则
 * 定义：不要存在多于一个导致类变更的原因。通俗的说，即一个类只负责一项职责。
 * 问题由来：类T负责两个不同的职责：职责P1，职责P2。当由于职责P1需求发生改变而需要修改类T时，
 * 有可能会导致原本运行正常的职责P2功能发生故障。
 * 解决方案：遵循单一职责原则。分别建立两个类T1、T2，使T1完成职责P1功能，T2完成职责P2功能。
 * 这样，当修改类T1时，不会使职责P2发生故障风险；
 * 同理，当修改T2时，也不会使职责P1发生故障风险。
 * 说到单一职责原则，很多人都会不屑一顾。因为它太简单了。稍有经验的程序员即使从来没有读过设计模式、从来没有听说过单一职责原则，
 * 在设计软件时也会自觉的遵守这一重要原则，因为这是常识。在软件编程中，谁也不希望因为修改了一个功能导致其他的功能发生故障。
 * 而避免出现这一问题的方法便是遵循单一职责原则。虽然单一职责原则如此简单，并且被认为是常识，但是即便是经验丰富的程序员写出的程序，
 * 也会有违背这一原则的代码存在。为什么会出现这种现象呢？因为有职责扩散。所谓职责扩散，就是因为某种原因，
 * 职责P被分化为粒度更细的职责P1和P2。
 * 比如：类T只负责一个职责P，这样设计是符合单一职责原则的。后来由于某种原因，也许是需求变更了，也许是程序的设计者境界提高了，
 * 需要将职责P细分为粒度更细的职责P1，P2，这时如果要使程序遵循单一职责原则，需要将类T也分解为两个类T1和T2，分别负责P1、P2两个职责。
 * 但是在程序已经写好的情况下，这样做简直太费时间了。所以，简单的修改类T，用它来负责两个职责是一个比较不错的选择，
 * 虽然这样做有悖于单一职责原则。
 * （这样做的风险在于职责扩散的不确定性，因为我们不会想到这个职责P，在未来可能会扩散为P1，P2，P3，P4……Pn。
 * 所以记住，在职责扩散到我们无法控制的程度之前，立刻对代码进行重构。）
 */
public class UnitTest {
    @Test
    public void main() {
//1
        动物 animal = new 动物();
        animal.breathe("牛");
        animal.breathe("羊");
        animal.breathe("猪");
//

        陆生动物 陆地 = new 陆生动物();
        陆地.breathe("牛");
        陆地.breathe("羊");
        陆地.breathe("猪");

        水生动物 水生 = new 水生动物();
        水生.breathe("鱼");



//        进化后的动物 animal2 = new 进化后的动物();
//        animal2.breathe("牛");
//        animal2.breathe("羊");
//        animal2.breathe("猪");
//        animal2.breathe("鱼");

    }

    class 动物 {
        public void breathe(String animal) {
            System.out.println(animal + "呼吸空气");
        }
    }

    class 进化后的动物 {
        public void breathe(String animal) {
            if ("鱼".equals(animal)) {
                System.out.println(animal + "呼吸水");
            } else {
                System.out.println(animal + "呼吸空气");
            }
        }
    }

    class 陆生动物 {
        public void breathe(String animal) {
            System.out.println(animal + "呼吸空气");
        }
    }

    class 水生动物 {
        public void breathe(String animal) {
            System.out.println(animal + "呼吸水");
        }
    }
}