/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import java.util.EnumSet;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with the virtual buttons on the RadioRA2 main repeater
 *
 * @author Bob Adair - Initial contribution
 */
public class VirtualKeypadHandler extends BaseKeypadHandler {

    private static enum COMPONENT implements KeypadComponent {
        BUTTON1(1, "button1"),
        BUTTON2(2, "button2"),
        BUTTON3(3, "button3"),
        BUTTON4(4, "button4"),
        BUTTON5(5, "button5"),
        BUTTON6(6, "button6"),
        BUTTON7(7, "button7"),
        BUTTON8(8, "button8"),
        BUTTON9(9, "button9"),
        BUTTON10(10, "button10"),
        BUTTON11(11, "button11"),
        BUTTON12(12, "button12"),
        BUTTON13(13, "button13"),
        BUTTON14(14, "button14"),
        BUTTON15(15, "button15"),
        BUTTON16(16, "button16"),
        BUTTON17(17, "button17"),
        BUTTON18(18, "button18"),
        BUTTON19(19, "button19"),
        BUTTON20(20, "button20"),
        BUTTON21(21, "button21"),
        BUTTON22(22, "button22"),
        BUTTON23(23, "button23"),
        BUTTON24(24, "button24"),
        BUTTON25(25, "button25"),
        BUTTON26(26, "button26"),
        BUTTON27(27, "button27"),
        BUTTON28(28, "button28"),
        BUTTON29(29, "button29"),
        BUTTON30(30, "button30"),
        BUTTON31(31, "button31"),
        BUTTON32(32, "button32"),
        BUTTON33(33, "button33"),
        BUTTON34(34, "button34"),
        BUTTON35(35, "button35"),
        BUTTON36(36, "button36"),
        BUTTON37(37, "button37"),
        BUTTON38(38, "button38"),
        BUTTON39(39, "button39"),
        BUTTON40(40, "button40"),
        BUTTON51(51, "button51"),
        BUTTON52(52, "button52"),
        BUTTON53(53, "button53"),
        BUTTON54(54, "button54"),
        BUTTON55(55, "button55"),
        BUTTON56(56, "button56"),
        BUTTON57(57, "button57"),
        BUTTON58(58, "button58"),
        BUTTON59(59, "button59"),
        BUTTON60(60, "button60"),
        BUTTON61(61, "button61"),
        BUTTON62(62, "button62"),
        BUTTON63(63, "button63"),
        BUTTON64(64, "button64"),
        BUTTON65(65, "button65"),
        BUTTON66(66, "button66"),
        BUTTON67(67, "button67"),
        BUTTON68(68, "button68"),
        BUTTON69(69, "button69"),
        BUTTON70(70, "button70"),
        BUTTON71(71, "button71"),
        BUTTON72(72, "button72"),
        BUTTON73(73, "button73"),
        BUTTON74(74, "button74"),
        BUTTON75(75, "button75"),
        BUTTON76(76, "button76"),
        BUTTON77(77, "button77"),
        BUTTON78(78, "button78"),
        BUTTON79(79, "button79"),
        BUTTON80(80, "button80"),
        BUTTON81(81, "button81"),
        BUTTON82(82, "button82"),
        BUTTON83(83, "button83"),
        BUTTON84(84, "button84"),
        BUTTON85(85, "button85"),
        BUTTON86(86, "button86"),
        BUTTON87(87, "button87"),
        BUTTON88(88, "button88"),
        BUTTON89(89, "button89"),
        BUTTON90(90, "button90"),
        BUTTON91(91, "button91"),
        BUTTON92(92, "button92"),
        BUTTON93(93, "button93"),
        BUTTON94(94, "button94"),
        BUTTON95(95, "button95"),
        BUTTON96(96, "button96"),
        BUTTON97(97, "button97"),
        BUTTON98(98, "button98"),
        BUTTON99(99, "button99"),
        BUTTON100(100, "button100"),

        LED1(101, "led1"),
        LED2(102, "led2"),
        LED3(103, "led3"),
        LED4(104, "led4"),
        LED5(105, "led5"),
        LED6(106, "led6"),
        LED7(107, "led7"),
        LED8(108, "led8"),
        LED9(109, "led9"),
        LED10(110, "led10"),
        LED11(111, "led11"),
        LED12(112, "led12"),
        LED13(113, "led13"),
        LED14(114, "led14"),
        LED15(115, "led15"),
        LED16(116, "led16"),
        LED17(117, "led17"),
        LED18(118, "led18"),
        LED19(119, "led19"),
        LED20(120, "led20"),
        LED21(121, "led21"),
        LED22(122, "led22"),
        LED23(123, "led23"),
        LED24(124, "led24"),
        LED25(125, "led25"),
        LED26(126, "led26"),
        LED27(127, "led27"),
        LED28(128, "led28"),
        LED29(129, "led29"),
        LED30(130, "led30"),
        LED31(131, "led31"),
        LED32(132, "led32"),
        LED33(133, "led33"),
        LED34(134, "led34"),
        LED35(135, "led35"),
        LED36(136, "led36"),
        LED37(137, "led37"),
        LED38(138, "led38"),
        LED39(139, "led39"),
        LED40(140, "led40"),
        LED41(141, "led41"),
        LED42(142, "led42"),
        LED43(143, "led43"),
        LED44(144, "led44"),
        LED45(145, "led45"),
        LED46(146, "led46"),
        LED47(147, "led47"),
        LED48(148, "led48"),
        LED49(149, "led49"),
        LED50(150, "led50"),
        LED51(151, "led51"),
        LED52(152, "led52"),
        LED53(153, "led53"),
        LED54(154, "led54"),
        LED55(155, "led55"),
        LED56(156, "led56"),
        LED57(157, "led57"),
        LED58(158, "led58"),
        LED59(159, "led59"),
        LED60(160, "led60"),
        LED61(161, "led61"),
        LED62(162, "led62"),
        LED63(163, "led63"),
        LED64(164, "led64"),
        LED65(165, "led65"),
        LED66(166, "led66"),
        LED67(167, "led67"),
        LED68(168, "led68"),
        LED69(169, "led69"),
        LED70(170, "led70"),
        LED71(171, "led71"),
        LED72(172, "led72"),
        LED73(173, "led73"),
        LED74(174, "led74"),
        LED75(175, "led75"),
        LED76(176, "led76"),
        LED77(177, "led77"),
        LED78(178, "led78"),
        LED79(179, "led79"),
        LED80(180, "led80"),
        LED81(181, "led81"),
        LED82(182, "led82"),
        LED83(183, "led83"),
        LED84(184, "led84"),
        LED85(185, "led85"),
        LED86(186, "led86"),
        LED87(187, "led87"),
        LED88(188, "led88"),
        LED89(189, "led89"),
        LED90(190, "led90"),
        LED91(191, "led91"),
        LED92(192, "led92"),
        LED93(193, "led93"),
        LED94(194, "led94"),
        LED95(195, "led95"),
        LED96(196, "led96"),
        LED97(197, "led97"),
        LED98(198, "led98"),
        LED99(199, "led99"),
        LED100(200, "led100");

        private final int id;
        private final String channel;

        COMPONENT(final int i, final String c) {
            id = i;
            channel = c;
        }

        @Override
        public int id() {
            return this.id;
        }

        @Override
        public String channel() {
            return this.channel;
        }

        public static boolean isLed(int id) {
            return (id >= 101 && id <= 200);
        }

        public static boolean isButton(int id) {
            return (id >= 1 && id <= 100);
        }

        public static boolean isCCI(int id) {
            return false;
        }
    }

    private Logger logger = LoggerFactory.getLogger(VirtualKeypadHandler.class);

    @Override
    protected void configureComponents(String model) {
        String mod = model == null ? "null" : model;
        this.logger.debug("Configuring components for keypad model {}", mod);

        switch (mod) {
            default:
                this.logger.warn("No valid model defined ({}). Assuming model RR-MAIN-REP.", mod);
            case "MAIN-REP":
                for (COMPONENT x : EnumSet.allOf(COMPONENT.class)) {
                    if (COMPONENT.isLed(x.id)) {
                        ledList.add(x);
                    }
                    if (COMPONENT.isButton(x.id)) {
                        buttonList.add(x);
                    }
                }
                break;
        }
    }

    public VirtualKeypadHandler(Thing thing) {
        super(thing);
        // TODO: Set flag to mark all channels as "Advanced"
    }

}
